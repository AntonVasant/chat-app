package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import database.DatabaseConnection;
import services.PasswordValidation;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
@WebServlet("/verifyotp")
public class VerifyOtpServlet extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse  response) throws IOException {
        BufferedReader bufferedReader = request.getReader();
        Gson gson = new Gson();
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("email");
        JsonObject jsonObject = gson.fromJson(bufferedReader, JsonObject.class);
        String otp = jsonObject.get("otp").getAsString();
        System.out.println(otp);
        System.out.println("in here "+email);
        boolean res = getOtpFromDb(email,otp);
        System.out.println(res);
        if (!res)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else{
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    private boolean getOtpFromDb(String email, String userOtp){
        String query = "SELECT * FROM otp WHERE email = ?";
        Connection connection  = DatabaseConnection.getConnection();
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1,email);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()){
                System.out.println("2");
                Timestamp current = new Timestamp(System.currentTimeMillis());
                Timestamp expire = result.getTimestamp(5);
                String otpDb =  result.getString("otp");
                String mail = result.getString("email");
                System.out.println(mail);
                System.out.println(otpDb);
                System.out.println("1");
                System.out.println(result);
                System.out.println(current.toString());
                System.out.println(expire.toString());
                System.out.println(userOtp);
                System.out.println("come last");
                System.out.println(expire.before(current));
                return (PasswordValidation.verifyPassword(userOtp,otpDb));
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
