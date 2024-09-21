import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;


@WebServlet("/chats")
public class ChatHistoryServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        HttpSession session = request.getSession(false);
        System.out.println(session.getAttribute("username"));
        String name = (String) session.getAttribute("username");
        System.out.println(name);
        System.out.println(session.getAttribute("id"));
        int organization_id = (int) session.getAttribute("organization");
        int id = (int) session.getAttribute("id");
        System.out.println(organization_id);
        System.out.println("pass "+organization_id);
        System.out.println("current id "+id);
        System.out.println("pr");

        String query = "SELECT * FROM chat_participants WHERE organization_id = (SELECT organization_id FROM chat_participants WHERE user_name = ?) AND user_name <> ?;";
        System.out.println("query");


        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, name);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                JSONArray chatsArray = new JSONArray();
                while (resultSet.next()) {
                    JSONObject chatObject = new JSONObject();
                    chatObject.put("id", resultSet.getInt(1));
                    chatObject.put("name",resultSet.getString(2));
                    chatObject.put("lastMessage",resultSet.getString(4));
                    chatObject.put("time",resultSet.getString(5));
                    chatsArray.put(chatObject);
                }
                response.setContentType("application/json");
                response.getWriter().write(chatsArray.toString());
            }
        } catch (SQLException e) {
            System.out.println("sql");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO");
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        doPost(request,response);
    }
}
