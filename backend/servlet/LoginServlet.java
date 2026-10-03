package servlet;

import dao.UserDAO;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {

        userDAO = new UserDAO();

    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String userId = request.getParameter("userId");
        String password = request.getParameter("password");


        // Validate input

        if (userId == null ||
            password == null ||
            userId.trim().isEmpty() ||
            password.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"User ID and password are required\"}"
            );

            return;
        }


        // Login check

        User user = userDAO.login(
                userId.trim(),
                password
        );


        // Login successful

        if (user != null) {

            HttpSession session =
                    request.getSession();

            session.setAttribute(
                    "userId",
                    user.getId()
            );

            session.setAttribute(
                    "loginUserId",
                    user.getUserId()
            );

            session.setAttribute(
                    "name",
                    user.getName()
            );

            session.setAttribute(
                    "role",
                    user.getRole()
            );

            session.setAttribute(
                    "department",
                    user.getDepartment()
            );

            session.setAttribute(
                    "batch",
                    user.getBatch()
            );


            response.setStatus(
                    HttpServletResponse.SC_OK
            );


            String jsonResponse =
                    "{"
                    + "\"success\":true,"
                    + "\"message\":\"Login successful\","
                    + "\"user\":{"
                    + "\"userId\":\""
                    + escapeJson(user.getUserId())
                    + "\","

                    + "\"name\":\""
                    + escapeJson(user.getName())
                    + "\","

                    + "\"role\":\""
                    + escapeJson(user.getRole())
                    + "\","

                    + "\"department\":\""
                    + escapeJson(user.getDepartment())
                    + "\","

                    + "\"batch\":\""
                    + escapeJson(user.getBatch())
                    + "\""

                    + "}"
                    + "}";


            response.getWriter().write(
                    jsonResponse
            );

        }


        // Login failed

        else {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Invalid User ID or password\"}"
            );
        }
    }


    // Escape JSON characters

    private String escapeJson(String value) {

        if (value == null) {

            return "";

        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}