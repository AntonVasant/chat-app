import org.json.JSONObject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

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
        String sender = jsonObject.getString("sender");
        int receiver = jsonObject.getInt("receiver");
        int id = userService.getUserIdByName(sender);
        String messageData = userService.getMessages(id,receiver);
        response.getWriter().write(messageData);
    }

    public void doGet(HttpServletRequest request,HttpServletResponse response) throws IOException {
    }
}
