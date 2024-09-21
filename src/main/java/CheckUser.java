import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.json.JSONObject;

@WebServlet("/checkUser")
public class CheckUser extends HttpServlet {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/servlet";
    private static final String JDBC_USER = "postgres";
    private static final String JDBC_PASSWORD = "Anton@2002";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder jsonBuffer = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }
        JSONObject jsonObject = new JSONObject(jsonBuffer.toString());
        String username = jsonObject.optString("username", "").trim();


        boolean exists = false;
        int id = -1;

        if (!username.isEmpty()) {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                String query = "SELECT COUNT(*) FROM users WHERE username = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, username);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            int count = resultSet.getInt(1);
                            exists = (count > 0);
                        }
                        if (exists){
                            HttpSession session = request.getSession();
                            session.setAttribute("receiver",username);
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("erroe");
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            out.write("{ \"exists\":" +exists +"}");
            out.flush();
        }
    }
}
