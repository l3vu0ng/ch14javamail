package murach.email;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import murach.business.User;
import murach.data.UserDB;
import murach.util.MailUtilGmail;
import murach.util.MailUtilLocal;

@WebServlet("/emailList")
public class EmailListServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Cập nhật currentYear cho footer
        GregorianCalendar currentDate = new GregorianCalendar();
        int currentYear = currentDate.get(Calendar.YEAR);
        request.setAttribute("currentYear", currentYear);

        // get current action
        String action = request.getParameter("action");
        if (action == null) {
            action = "join";  // default action
        }

        // perform action and set URL to appropriate page
        String url = "/index.jsp";
        if (action.equals("join")) {
            url = "/index.jsp";    // the "join" page
        } else if (action.equals("add")) {
            // get parameters from the request
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");

            // store data in User object and insert into database
            User user = new User(firstName, lastName, email);
            UserDB.insert(user);
            request.setAttribute("user", user);

            // send email to user
            String to = email;
            String from = "volevuong2006@gmail.com";

            // Nếu người dùng cấu hình tài khoản Gmail khác từ biến môi trường
            String envUser = System.getenv("GMAIL_USERNAME");
            if (envUser != null && !envUser.isBlank()) {
                from = envUser;
            }

            String subject = "Welcome to our email list";
            String body = "Dear " + firstName + ",\n\n"
                    + "Thanks for joining our email list. "
                    + "We'll make sure to send "
                    + "you announcements about new products "
                    + "and promotions.\n"
                    + "Have a great day and thanks again!\n\n"
                    + "Kelly Slivkoff\n"
                    + "Mike Murach & Associates";
            boolean isBodyHTML = false;

            // Kiểm tra tùy chọn gửi mail (mặc định gửi Gmail thực tế theo Slide 27-28)
            String mailMode = request.getParameter("mailMode"); // "local" hoặc "gmail"
            try {
                if ("local".equalsIgnoreCase(mailMode)) {
                    MailUtilLocal.sendMail(to, from, subject, body, isBodyHTML);
                } else {
                    MailUtilGmail.sendMail(to, from, subject, body, isBodyHTML);
                }
                request.setAttribute("mailSuccess", "Email xác nhận đã được gửi thành công đến: " + to);
            } catch (MessagingException e) {
                String errorMessage
                        = "ERROR: Unable to send email.<br>"
                        + "Check Tomcat logs for details.<br>"
                        + "NOTE: You may need to configure your system "
                        + "as described in chapter 14.<br>"
                        + "ERROR MESSAGE: " + e.getMessage();
                request.setAttribute("errorMessage", errorMessage);
                this.log(
                        "Unable to send email. \n"
                        + "Here is the email you tried to send: \n"
                        + "=====================================\n"
                        + "TO: " + email + "\n"
                        + "FROM: " + from + "\n"
                        + "SUBJECT: " + subject + "\n\n"
                        + body + "\n\n");
            }
            url = "/thanks.jsp";
        }

        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
