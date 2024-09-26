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


    public void saveMessageToDatabase(int sender, int receiver, String content, boolean state) {
        System.out.println("saving");
        int id = findChatId(sender,receiver);
        System.out.println(id);
        Connection connection = DatabaseConnection.getConnection();
        String insertMessageSQL = "INSERT INTO messages (sender_id, receiver_id, chat_id, message_text, is_read)\n" +
                "VALUES (?, ?, ?, ?, ?);";
            try (PreparedStatement insertMessageStmt = connection.prepareStatement(insertMessageSQL)) {
                insertMessageStmt.setInt(1, sender);
                insertMessageStmt.setInt(2, receiver);
                insertMessageStmt.setInt(3,id);
                insertMessageStmt.setString(4, content);
                insertMessageStmt.setBoolean(5,state);
                insertMessageStmt.executeUpdate();
            }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<JSONObject> getMessages(int chatId) {

        String query = "SELECT *  \n" +
                "FROM messages m\n" +
                "WHERE m.chat_id = ?\n" +
                "ORDER BY m.created_at ASC;";
        List<JSONObject> messages = new ArrayList<>();

        Connection connection = DatabaseConnection.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, chatId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                do {
                    JSONObject messageJson = new JSONObject();
                    messageJson.put("sender", rs.getInt(3));
                    messageJson.put("receiverId", rs.getInt(4));
                    messageJson.put("content", rs.getString(5));
                    messageJson.put("timestamp", rs.getTimestamp(7));
                    messages.add(messageJson);
                } while (rs.next());
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return messages;
    }


    public int findChatId(int sender, int receiver){
        String query = "SELECT * FROM chats WHERE user1_id = ? AND user2_id = ? OR user1_id = ? AND user2_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1,sender);
            preparedStatement.setInt(2,receiver);
            preparedStatement.setInt(3,receiver);
            preparedStatement.setInt(4,sender);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public String grabAllUnReadMessages(int id) {
        String query = "SELECT\n" +
                "    m.message_id,\n" +
                "    m.message_text,\n" +
                "    m.created_at,\n" +
                "    u.name AS sender_name\n" +
                "FROM messages m\n" +
                "INNER JOIN users u ON m.sender_id = u.user_id\n" +
                "WHERE m.receiver_id = ? AND m.is_read = FALSE;";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            JSONArray messages = new JSONArray();
            while (rs.next()) {
                JSONObject messageJson = new JSONObject();
                messageJson.put("sender",rs.getString("sender_name"));
                messageJson.put("content", rs.getString("message_text"));
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

    public void createNewChat(int user1, int user2) throws SQLException {
        String query = "INSERT INTO chats (user1_id, user2_id) VALUES (?,?);";
        Connection connection = DatabaseConnection.getConnection();
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1,user1);
            statement.setInt(2,user2);
            statement.executeUpdate();
        }
    }
}