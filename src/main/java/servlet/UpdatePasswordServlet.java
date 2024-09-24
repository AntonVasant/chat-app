package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import services.PasswordResetService;
import services.PasswordValidation;
import services.UserService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/updatePassword")
public class UpdatePasswordServlet extends HttpServlet {

    private final UserService userService = UserService.getUserService();

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader bufferedReader = request.getReader();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(bufferedReader, JsonObject.class);
        String password = jsonObject.get("password").getAsString();
        System.out.println("before");
        System.out.println(password);
        password = PasswordValidation.hashPassword(password);
        System.out.println("after");
        System.out.println(password);
        HttpSession session = request.getSession();
        String mail = (String) session.getAttribute("email");
        boolean result = userService.updatePassword(password,mail);
        if (!result)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        else{
            PasswordResetService service = PasswordResetService.getPasswordResetService();
            service.deleteOtp(mail);
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}
