package services;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

    private Properties emailProperties;

    private static EmailService emailService;

    public static EmailService getInstance(){
        if (emailService == null){
            emailService = new EmailService();
        }
        return emailService;
    }

    public EmailService() {
        emailProperties = new Properties();
        try {
            FileInputStream fis = new FileInputStream("C:\\Users\\Hp\\Desktop\\security\\src\\main\\resources\\email-config.properties");
            emailProperties.load(fis);
            System.out.println(emailProperties.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendOtpEmail(String toEmail, String otp) throws MessagingException {
        String domain = extractDomain(toEmail);
        System.out.println(domain);
        Properties smtpProps = setupSmtpProperties(domain);

        String fromEmail = emailProperties.getProperty("email.default.from");
        String password = emailProperties.getProperty("email.default.password");
        Session session = Session.getInstance(smtpProps, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
        String htmlContent = extractHtml("C:\\Users\\Hp\\Desktop\\security\\src\\main\\resources\\otp-email-template.html");
        htmlContent = htmlContent.replace("{{OTP}}",otp);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Password Reset OTP");
        message.setContent(htmlContent,"text/html");
        Transport.send(message);
        System.out.println("OTP Email sent successfully!");
    }


    private String extractHtml(String filePath){
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return contentBuilder.toString();
    }

    private String extractDomain(String email) {
        return email.substring(email.indexOf("@") + 1);
    }

    private Properties setupSmtpProperties(String domain) {
        Properties smtpProps = new Properties();
        switch (domain) {
            case "gmail.com":
                smtpProps.put("mail.smtp.host", emailProperties.getProperty("gmail.host"));
                smtpProps.put("mail.smtp.port", emailProperties.getProperty("gmail.port"));
                smtpProps.put("mail.smtp.auth", emailProperties.getProperty("gmail.auth"));
                smtpProps.put("mail.smtp.starttls.enable", emailProperties.getProperty("gmail.starttls.enable"));
                smtpProps.put("mail.smtp.ssl.protocols","TLSv1.2");
                break;
            default:
                throw new IllegalArgumentException("Unsupported email domain: " + domain);
        }
        return smtpProps;
    }
}

