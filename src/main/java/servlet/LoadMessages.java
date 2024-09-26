package servlet;

import org.json.JSONObject;
import services.UserService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/load")
public class LoadMessages extends HttpServlet {

    private final UserService userService = UserService.getUserService();
    public void doPost(HttpServletRequest request , HttpServletResponse response) throws IOException {
        StringBuilder jsonBuffer = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        JSONObject jsonObject = new JSONObject(jsonBuffer.toString());
        int sender = jsonObject.getInt("sender");
        int receiver = jsonObject.getInt("receiver");
        int chatId = userService.findChatId(sender, receiver);
        if (chatId > 0 ){
            String messageData = userService.getMessages(chatId).toString();
            response.getWriter().write(messageData);
        }
        else {
            try {
                System.out.println("else");
                userService.createNewChat(sender,receiver);
                response.getWriter().write("no messages");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
