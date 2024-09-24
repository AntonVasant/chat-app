package notification;

import database.DatabaseConnection;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/notification")
public class NotificationManager extends HttpServlet {

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
        String senderName  = jsonObject.getString("senderName");
        String receiverName = jsonObject.getString("receiverName");

        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "UPDATE messages SET message_status = 'read' WHERE sender = ? AND receiver = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, senderName);
            pstmt.setString(2,receiverName);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
