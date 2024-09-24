package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import database.DatabaseConnection;
import services.EmailService;
import services.PasswordValidation;
import services.UserService;

import javax.mail.MessagingException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@WebServlet("/reset")
public class RestPassword extends HttpServlet {

    private final UserService userService = UserService.getUserService();

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("servlet");
        BufferedReader bufferedReader = request.getReader();
        HttpSession session = request.getSession(true);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(bufferedReader, JsonObject.class);
        String email = jsonObject.get("email").getAsString();
        session.setAttribute("email",email);
        boolean exist = userService.verifyUserByEmail(email);
        if (!exist){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String otp = generateOTP(6);
        EmailService emailService = EmailService.getInstance();
        try {
            emailService.sendOtpEmail(email,otp);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        otp = PasswordValidation.hashPassword(otp);
        saveOtp(otp,email);
        System.out.println("email sent");
        response.setStatus(HttpServletResponse.SC_OK);
        System.out.println(response.getStatus());
    }

    public String generateOTP(int length) {
        String numbers = "0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(numbers.length());
            otp.append(numbers.charAt(index));
        }
        return otp.toString();
    }

    private void saveOtp(String otp, String mail){
        String query = "INSERT INTO otp (email, otp, created_at, expires_at) VALUES (?, ?, ?, ?);";
        Connection connection = DatabaseConnection.getConnection();
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1,mail);
            preparedStatement.setString(2,otp);
            preparedStatement.setTimestamp(3,new Timestamp(System.currentTimeMillis()));
            Timestamp expire = new Timestamp(System.currentTimeMillis() + (3*60*1000));
            preparedStatement.setTimestamp(4,expire);
            int n = preparedStatement.executeUpdate();
            System.out.println(n);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
