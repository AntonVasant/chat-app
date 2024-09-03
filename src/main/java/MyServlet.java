
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/login")
public class MyServlet extends HttpServlet {
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/servlet";
    private static final String JDBC_USER = "postgres";
    private static final String JDBC_PASSWORD = "Anton@2002";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Successful login
                        HttpSession session = request.getSession();
                        session.setAttribute("username", username);
                        response.sendRedirect("welcome.jsp"); // Redirect to a welcome page or dashboard
                    } else {
                        // Failed login
                        response.setContentType("text/html");
                        try (PrintWriter out = response.getWriter()) {
                            out.println("<html><body>");
                            out.println("<h3>Invalid username or password!</h3>");
                            out.println("<a href='login.html'>Try again</a>");
                            out.println("</body></html>");
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException("Login failed", e);
        }
    }
}