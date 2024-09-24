package servlet;

import database.DatabaseConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import services.PasswordValidation;
import services.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserService userService = UserService.getUserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuilder jsonBuffer = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        JSONObject jsonObject = new JSONObject(jsonBuffer.toString());
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");
        response.setContentType("application/json");
        System.out.println(username);
        System.out.println(password);

        HttpSession session;

            Connection connection = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM users WHERE user_name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);
                JSONObject jsonObject1 = new JSONObject();
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String hashedPassword = resultSet.getString("hashed_password");
                        String role = resultSet.getString("role");
                        int id = resultSet.getInt(1);
                        int organization_id =  resultSet.getInt(5);
                        session = request.getSession(true);
                        if (PasswordValidation.verifyPassword(password,hashedPassword)){
                            String messagesJson = userService.grabAllUnReadMessages(id);
                            if (messagesJson != null){
                                session.setAttribute("username", username);
                                session.setAttribute("role", role);
                                session.setAttribute("id",id);
                                session.setAttribute("organization",organization_id);
                                JSONArray jsonArray = new JSONArray(messagesJson);
                                jsonObject1.put("notification",jsonArray);
                                jsonObject1.put("loggedIn",true);
                                jsonObject1.put("role",role);
                                response.getWriter().write(jsonObject1.toString());
                            }
                        }
                    } else {
                        jsonObject1.put("role","none");

                    }
                }
            }
         catch (Exception e) {
            throw new ServletException("Login failed", e);
        }
    }

}
