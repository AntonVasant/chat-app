import org.json.JSONArray;
import org.json.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@WebServlet("/login")
public class MyServlet extends HttpServlet {

    private final  UserService userService = UserService.getUserService();


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        JSONObject jsonObject = new JSONObject();
        if (session != null && session.getAttribute("username") != null) {
            String role = (String) session.getAttribute("role");
            jsonObject.put("loggedIn",true);
            jsonObject.put("role",role);
        }
        else jsonObject.put("loggedIn",true);
        response.getWriter().write(jsonObject.toString());
    }

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
                        System.out.println("messagesss "+id);
                        int organization_id =  resultSet.getInt(5);
                        session = request.getSession(true);
                        session.setAttribute("username", username);
                        session.setAttribute("role", role);
                        session.setAttribute("id",id);
                        session.setAttribute("organization",organization_id);
                        System.out.println(session.getAttribute("username"));
                        System.out.println(session.getAttribute("organization"));

                        if (PasswordValidation.verifyPassword(password,hashedPassword)){
                            System.out.println("password");
                            String messagesJson = userService.grabAllUnReadMessages(id);
                            if (messagesJson != null){
                                System.out.println(messagesJson);
                                JSONArray jsonArray = new JSONArray(messagesJson);
                                jsonObject1.put("notification",jsonArray);
                                jsonObject1.put("loggedIn",true);
                                jsonObject1.put("role",role);
                                System.out.println(jsonObject1);
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
