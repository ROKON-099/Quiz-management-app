package servlet;

import dao.NoticeDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/create-notice")
public class CreateNoticeServlet extends HttpServlet {

    private NoticeDAO noticeDAO;

    @Override
    public void init() throws ServletException {

        noticeDAO = new NoticeDAO();

    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        HttpSession session =
                request.getSession(false);


        // Check login

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().write(
                    """
                    {
                        "success": false,
                        "message": "Not logged in"
                    }
                    """
            );

            return;
        }


        // Teacher database ID

        int teacherId =
                (Integer) session.getAttribute("userId");


        // Form data

        String topic =
                request.getParameter("topic");

        String message =
                request.getParameter("message");


        // Validation

        if (topic == null ||
            message == null ||
            topic.trim().isEmpty() ||
            message.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    """
                    {
                        "success": false,
                        "message": "Title and message are required"
                    }
                    """
            );

            return;
        }


        // Create notice

        boolean success =
                noticeDAO.createNotice(
                        teacherId,
                        topic.trim(),
                        message.trim()
                );


        if (success) {

            response.setStatus(
                    HttpServletResponse.SC_OK
            );

            response.getWriter().write(
                    """
                    {
                        "success": true,
                        "message": "Notice sent successfully"
                    }
                    """
            );

        } else {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    """
                    {
                        "success": false,
                        "message": "Failed to create notice"
                    }
                    """
            );
        }
    }
}