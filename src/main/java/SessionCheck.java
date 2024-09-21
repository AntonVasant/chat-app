import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/session")
public class SessionCheck extends HttpServlet {
   private final UserService userService = UserService.getUserService();
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            HttpSession session = request.getSession(false);
            boolean loggedIn = (session != null);
            int id = loggedIn ? (int) session.getAttribute("id") : -1;
            String role = loggedIn ? (String) session.getAttribute("role") : null;
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("loggedIn", loggedIn);
            jsonResponse.put("role", role != null ? role : "");
            if (loggedIn && id != -1) {
                String messagesJson = userService.grabAllUnReadMessages(id);
                jsonResponse.put("messages", messagesJson);
            }

            response.setContentType("application/json");

            try (PrintWriter out = response.getWriter()) {
                out.write(jsonResponse.toString());
                out.flush();
            }
        }

}