package servlet;

import database.DatabaseConnection;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/getChatId")
public class FindChatId extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder jsonBuffer = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        JSONObject jsonObject = new JSONObject(jsonBuffer.toString());
        int user1 = jsonObject.getInt("sender");
        int user2 = jsonObject.getInt("receiver");
        String query  = "SELECT * FROM chats WHERE user1_id = ? AND user2_id = ? OR user1_id = ? AND user2_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1,user1);
            statement.setInt(2,user2);
            statement.setInt(3,user2);
            statement.setInt(4,user1);
            ResultSet resultSet = statement.executeQuery();
            int chatId = resultSet.getInt(1);
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("chatId",chatId);
            response.getWriter().write(jsonObject1.toString());
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
