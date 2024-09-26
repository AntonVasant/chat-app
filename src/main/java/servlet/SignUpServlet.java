package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import database.DatabaseConnection;
import services.PasswordValidation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;

@WebServlet("/signup")
public class SignUpServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        BufferedReader bufferedReader = request.getReader();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(bufferedReader, JsonObject.class);
        String username = jsonObject.get("username").getAsString();
        String password =  jsonObject.get("password").getAsString();
        String email = jsonObject.get("email").getAsString();
        String role =  jsonObject.get("role").getAsString();
        int organization =  jsonObject.get("organization_id").getAsInt();
        System.out.println(username+" "+password+" "+email+" "+role+" "+organization);
        if (username == null || password == null || role == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing required parameters.");
            return;
        }

        password = PasswordValidation.hashPassword(password);

        String sqlCheck = "SELECT * FROM users WHERE name = ?";
        String sqlInsert = "INSERT INTO users (name, password, role, email, organization_id) VALUES (?, ?, ?, ?, ?)";


        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatementCheck = connection.prepareStatement(sqlCheck);
             PreparedStatement preparedStatementInsert = connection.prepareStatement(sqlInsert))
        {

            preparedStatementCheck.setString(1, username);

            try (ResultSet resultSet = preparedStatementCheck.executeQuery()) {
                if (resultSet.next()) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.getWriter().write("User already exists.");
                } else {
                    preparedStatementInsert.setString(1, username);
                    preparedStatementInsert.setString(2, password);
                    preparedStatementInsert.setString(3, role);
                    preparedStatementInsert.setString(4,email);
                    preparedStatementInsert.setInt(5, organization);
                    int rowsAffected = preparedStatementInsert.executeUpdate();
                    if (rowsAffected > 0) {
                        response.setStatus(HttpServletResponse.SC_CREATED);
                        response.getWriter().write("User created successfully.");
                    } else {
                        System.out.println("fpond");
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        response.getWriter().write("Failed to create user.");
                    }
                }
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("Database error: " + e.getMessage());
        }
    }
}