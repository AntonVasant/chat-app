import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
        if (username == null || password == null || role == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing required parameters.");
            return;
        }

        password = PasswordValidation.hashPassword(password);

        String sqlCheck = "SELECT * FROM users WHERE user_name = ?";
        String sqlInsert = "INSERT INTO users (user_name, hashed_password, role, email, organization_id) VALUES (?, ?, ?, ?, ?)";
        String chat  = "INSERT INTO chat_participants (user_name, organization_id)  VALUES (?,?)";


        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatementCheck = connection.prepareStatement(sqlCheck);
             PreparedStatement preparedStatementInsert = connection.prepareStatement(sqlInsert);
             PreparedStatement preparedStatement = connection.prepareStatement(chat))
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
                    preparedStatement.setString(1,username);
                    preparedStatement.setInt(2,organization);
                    int rowsAffected = preparedStatementInsert.executeUpdate();
                    int n = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        response.setStatus(HttpServletResponse.SC_CREATED);
                        response.getWriter().write("User created successfully.");
                    } else {
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