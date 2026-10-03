package servlet;

import dao.NoticeDAO;
import model.Notice;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/notices")
public class NoticeServlet extends HttpServlet {

    private NoticeDAO noticeDAO;

    @Override
    public void init() throws ServletException {
        noticeDAO = new NoticeDAO();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Not logged in\"}"
            );

            return;
        }

        List<Notice> notices =
                noticeDAO.getAllNotices();

        StringBuilder json =
                new StringBuilder();

        json.append("{\"success\":true,\"notices\":[");

        for (int i = 0; i < notices.size(); i++) {

            Notice notice = notices.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{");

            json.append("\"id\":")
                    .append(notice.getId())
                    .append(",");

            json.append("\"teacherId\":")
                    .append(notice.getTeacherId())
                    .append(",");

            json.append("\"topic\":\"")
                    .append(escape(notice.getTopic()))
                    .append("\",");

            json.append("\"message\":\"")
                    .append(escape(notice.getMessage()))
                    .append("\",");

            json.append("\"quizDate\":\"")
                    .append(escape(notice.getQuizDate()))
                    .append("\",");

            json.append("\"quizTime\":\"")
                    .append(escape(notice.getQuizTime()))
                    .append("\"");

            json.append("}");
        }

        json.append("]}");

        response.getWriter().write(
                json.toString()
        );
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}