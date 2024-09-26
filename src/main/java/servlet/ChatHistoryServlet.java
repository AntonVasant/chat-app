package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;

import database.DatabaseConnection;
import org.json.JSONArray;
import org.json.JSONObject;


@WebServlet("/api/chats")
public class ChatHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        HttpSession session = request.getSession(false);
        System.out.println("name");
        String send =  request.getParameter("id");
        int sender = Integer.parseInt(send);
        int id = (int) session.getAttribute("id");
        System.out.println("sender "+sender);

        String query = "SELECT u.user_id, u.name, u.email, u.role, \n" +
                "       COALESCE(COUNT(m.message_id), 0) AS unread_messages\n" +
                "FROM users u\n" +
                "LEFT JOIN chats c \n" +
                "       ON (c.user1_id = ? AND c.user2_id = u.user_id)\n" +
                "       OR (c.user2_id = ? AND c.user1_id = u.user_id)\n" +
                "LEFT JOIN messages m \n" +
                "       ON m.chat_id = c.chat_id\n" +
                "       AND m.receiver_id = ? \n" +
                "       AND m.is_read = FALSE\n" +
                "WHERE u.organization_id = (SELECT organization_id FROM users WHERE user_id = ?)\n" +
                "AND u.user_id != ?\n" +
                "GROUP BY u.user_id, u.name, u.email, u.role;\n";


        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, id);
            preparedStatement.setInt(3, id);
            preparedStatement.setInt(4, id);
            preparedStatement.setInt(5, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                JSONArray jsonArray = new JSONArray();
                while (resultSet.next()) {
                    JSONObject jsonObject = new JSONObject();

                    int userId = resultSet.getInt("user_id");
                    String name = resultSet.getString("name");
                    String email = resultSet.getString("email");
                    int unreadMessages = resultSet.getInt("unread_messages");

                    jsonObject.put("userId", userId);
                    jsonObject.put("name", name);
                    jsonObject.put("email", email);
                    jsonObject.put("unread_messages", unreadMessages);

                    jsonArray.put(jsonObject);
                }

                    response.setContentType("application/json");
                response.getWriter().write(jsonArray.toString());
        } catch (SQLException e) {
            System.out.println("sql");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO");
            e.printStackTrace();
        }
    } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
