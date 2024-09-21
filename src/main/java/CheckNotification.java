import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
@WebServlet("/unreadMessages")
public class CheckNotification extends HttpServlet {

    private final UserService userService = UserService.getUserService();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        int id  = (int) session.getAttribute("id");
        String messages = userService.grabAllUnReadMessages(id);
        if (messages != null){
            response.getWriter().write(messages);
        }
    }
}
