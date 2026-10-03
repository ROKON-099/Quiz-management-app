package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/result")
public class ResultServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // =========================
        // SESSION CHECK
        // =========================

        HttpSession session =
                request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(401);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Please login first\"}"
            );

            return;
        }

        // =========================
        // STUDENT CHECK
        // =========================

        if (!"STUDENT".equals(
                session.getAttribute("role"))) {

            response.setStatus(403);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Student access required\"}"
            );

            return;
        }

        int studentId =
                (Integer) session.getAttribute("userId");

        // =========================
        // QUIZ ID
        // =========================

        String quizIdParam =
                request.getParameter("quizId");

        if (quizIdParam == null ||
            quizIdParam.trim().isEmpty()) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Quiz ID is missing\"}"
            );

            return;
        }

        int quizId;

        try {

            quizId =
                    Integer.parseInt(quizIdParam);

        } catch (NumberFormatException e) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid Quiz ID\"}"
            );

            return;
        }

        // =========================
        // GET RESULT
        // =========================

        String sql = """
            SELECT
                qr.score,
                qr.total,
                qr.submitted_at,
                q.topic,
                s.name AS subject_name
            FROM quiz_results qr
            JOIN quizzes q
                ON qr.quiz_id = q.id
            JOIN subjects s
                ON q.subject_id = s.id
            WHERE qr.student_id = ?
              AND qr.quiz_id = ?
            LIMIT 1
            """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, studentId);
            statement.setInt(2, quizId);

            try (ResultSet rs =
                    statement.executeQuery()) {

                if (!rs.next()) {

                    response.setStatus(404);

                    response.getWriter().write(
                        "{\"success\":false,\"message\":\"Result not found\"}"
                    );

                    return;
                }

                int score =
                        rs.getInt("score");

                int total =
                        rs.getInt("total");

                String topic =
                        rs.getString("topic");

                String subjectName =
                        rs.getString("subject_name");

                String submittedAt =
                        rs.getString("submitted_at");

                double percentage = 0;

                if (total > 0) {
                    percentage =
                            (score * 100.0) / total;
                }

                String json =
                    "{"
                    + "\"success\":true,"
                    + "\"score\":" + score + ","
                    + "\"total\":" + total + ","
                    + "\"percentage\":" + percentage + ","
                    + "\"topic\":\""
                    + escape(topic)
                    + "\","
                    + "\"subjectName\":\""
                    + escape(subjectName)
                    + "\","
                    + "\"submittedAt\":\""
                    + escape(submittedAt)
                    + "\""
                    + "}";

                response.getWriter().write(json);
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(500);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Server error while loading result\"}"
            );
        }
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}