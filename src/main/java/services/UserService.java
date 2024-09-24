package services;

import database.DatabaseConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    static  UserService userService;

    public static UserService getUserService(){
        if (userService == null){
            userService = new UserService();
        }
        return userService;
    }


    public void saveMessageToDatabase(String sender, String receiver, String content) {
        int id1 = getUserIdByName(sender);
        int id2 = getUserIdByName(receiver);
        Connection connection = DatabaseConnection.getConnection();
        String insertMessageSQL = "INSERT INTO messages (sender_id, receiver_id, content, sender, receiver) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertMessageStmt = connection.prepareStatement(insertMessageSQL)) {
                insertMessageStmt.setInt(1, id1);
                insertMessageStmt.setInt(2, id2);
                insertMessageStmt.setString(3, content);
                insertMessageStmt.setString(4, sender);
                insertMessageStmt.setString(5, receiver);
                insertMessageStmt.executeUpdate();
            }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getMessages(int senderId, int receiverId) {
        String query = "SELECT * FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY created_at;";
        List<JSONObject> messages = new ArrayList<>();

        Connection connection = DatabaseConnection.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, senderId);
            statement.setInt(2, receiverId);
            statement.setInt(3, receiverId);
            statement.setInt(4, senderId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                JSONObject messageJson = new JSONObject();
                messageJson.put("senderId",rs.getInt(3));
                messageJson.put("senderName",rs.getString("sender"));
                messageJson.put("receiverName",rs.getString("receiver"));
                messageJson.put("receiver", rs.getInt(4));
                messageJson.put("content", rs.getString(5));
                messageJson.put("timestamp", rs.getTimestamp(6).toString());
                messages.add(messageJson);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = new JSONArray(messages);
        return jsonArray.toString();
    }

    public String grabAllUnReadMessages(int id) {
        String query = "SELECT * FROM messages WHERE receiver_id = ? AND message_status = 'sent'";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            JSONArray messages = new JSONArray();
            while (rs.next()) {
                JSONObject messageJson = new JSONObject();
                messageJson.put("sender",rs.getString("sender"));
                messageJson.put("content", rs.getString("content"));
                messageJson.put("timestamp", rs.getTimestamp("created_at").toString());
                messages.put(messageJson);
            }
            return messages.toString();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public String getUserNameById(int id) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String name = rs.getString("user_name");
                System.out.println("username " + name);
                return name;
            } else {
                System.out.println("User not found");
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public int getUserIdByName(String name) {
        String query = "SELECT * FROM users WHERE user_name = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int id = rs.getInt(1);
                return id;
            } else {
                System.out.println("User not found");
            }
            return 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifyUserByEmail(String email) {

        String query = "SELECT * FROM users WHERE email = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1,email);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePassword(String password,String mail){
        String query = "UPDATE users SET hashed_password = ? WHERE email = ?";
        Connection connection = DatabaseConnection.getConnection();
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1,password);
            preparedStatement.setString(2,mail);
            int n = preparedStatement.executeUpdate();
            return n > 0;
        }catch (SQLException e){
            e.printStackTrace();
        }
       return false;
    }
}