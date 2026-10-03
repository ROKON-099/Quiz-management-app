package servlet;

import dao.NoticeDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/update-notice")
public class UpdateNoticeServlet extends HttpServlet {

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


        HttpSession session =
                request.getSession(false);


        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(401);

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Not logged in\"}"
            );

            return;
        }


        int teacherId =
                (Integer) session.getAttribute("userId");


        int id =
                Integer.parseInt(
                        request.getParameter("id")
                );


        String topic =
                request.getParameter("topic");

        String message =
                request.getParameter("message");


        boolean success =
                noticeDAO.updateNotice(
                        id,
                        teacherId,
                        topic,
                        message
                );


        response.getWriter().write(
                success
                ? "{\"success\":true}"
                : "{\"success\":false,\"message\":\"Update failed\"}"
        );
    }
}