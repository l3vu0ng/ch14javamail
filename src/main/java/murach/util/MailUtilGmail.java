package murach.util;

import java.util.Properties;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class MailUtilGmail {

    public static void sendMail(String to, String from,
            String subject, String body, boolean bodyIsHTML)
            throws MessagingException {

        // Lấy thông tin xác thực từ biến môi trường hoặc dùng tài khoản cấu hình mặc định
        String username = System.getenv("GMAIL_USERNAME");
        if (username == null || username.isBlank()) {
            username = System.getProperty("mail.gmail.username", "volevuong2006@gmail.com");
        }

        String password = System.getenv("GMAIL_APP_PASSWORD");
        if (password == null || password.isBlank()) {
            password = System.getProperty("mail.gmail.password", "ahoiutdmolpwqpcn");
        }

        sendMail(to, from, subject, body, bodyIsHTML, username, password);
    }

    public static void sendMail(String to, String from,
            String subject, String body, boolean bodyIsHTML,
            String username, String password)
            throws MessagingException {

        // 1 - get a mail session (theo chuẩn Slide 27-28 & tương thích Gmail SSL hiện đại)
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", "smtp.gmail.com");
        props.put("mail.smtps.port", "465");
        props.put("mail.smtps.auth", "true");
        props.put("mail.smtps.quitwait", "false");
        props.put("mail.smtps.ssl.enable", "true");
        props.put("mail.smtps.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props);
        session.setDebug(true);

        // 2 - create a message
        Message message = new MimeMessage(session);
        message.setSubject(subject);
        if (bodyIsHTML) {
            message.setContent(body, "text/html; charset=UTF-8");
        } else {
            message.setText(body);
        }

        // 3 - address the message
        Address fromAddress = new InternetAddress(from);
        Address toAddress = new InternetAddress(to);
        message.setFrom(fromAddress);
        message.setRecipient(Message.RecipientType.TO, toAddress);

        // 4 - send the message
        Transport transport = session.getTransport();
        transport.connect(username, password);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }
}
