package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import util.DBConnection;

@WebServlet("/leaderboard")
public class LeaderboardServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        // =========================================
        // SESSION CHECK
        // =========================================

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            out.print("""
                {
                    "success": false,
                    "message": "Please login first."
                }
            """);

            return;
        }


        // =========================================
        // LEADERBOARD QUERY
        // Student + Teacher both can see
        // =========================================

        String sql = """
            SELECT
                u.id,
                u.user_id,
                u.name,
                u.batch,

                COUNT(qr.id) AS quiz_count,

                COALESCE(
                    SUM(qr.score),
                    0
                ) AS total_score,

                COALESCE(
                    AVG(
                        CASE
                            WHEN qr.total > 0
                            THEN (qr.score * 100.0 / qr.total)
                            ELSE 0
                        END
                    ),
                    0
                ) AS average_mark

            FROM users u

            LEFT JOIN quiz_results qr
                ON qr.student_id = u.id

            WHERE u.role = 'STUDENT'

            GROUP BY
                u.id,
                u.user_id,
                u.name,
                u.batch

            ORDER BY
                average_mark DESC,
                total_score DESC
        """;


        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            StringBuilder json = new StringBuilder();

            json.append("{");
            json.append("\"success\":true,");
            json.append("\"students\":[");

            int rank = 1;

            while (rs.next()) {

                if (rank > 1) {
                    json.append(",");
                }

                json.append("{");

                json.append("\"rank\":")
                    .append(rank)
                    .append(",");

                json.append("\"name\":\"")
                    .append(escape(rs.getString("name")))
                    .append("\",");

                json.append("\"userId\":\"")
                    .append(escape(rs.getString("user_id")))
                    .append("\",");

                json.append("\"batch\":\"")
                    .append(escape(rs.getString("batch")))
                    .append("\",");

                json.append("\"quizCount\":")
                    .append(rs.getInt("quiz_count"))
                    .append(",");

                json.append("\"totalScore\":")
                    .append(rs.getInt("total_score"))
                    .append(",");

                json.append("\"averageMark\":")
                    .append(
                        String.format(
                            java.util.Locale.US,
                            "%.2f",
                            rs.getDouble("average_mark")
                        )
                    );

                json.append("}");

                rank++;
            }

            json.append("]");
            json.append("}");

            out.print(json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print("""
                {
                    "success": false,
                    "message": "Database error while loading leaderboard."
                }
            """);
        }
    }


    // =========================================
    // JSON ESCAPE
    // =========================================

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