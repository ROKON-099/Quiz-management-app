package servlet;

import dao.QuizDAO;
import model.Quiz;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/student-quizzes")
public class StudentQuizServlet extends HttpServlet {

    private QuizDAO quizDAO;

    @Override
    public void init() throws ServletException {
        quizDAO = new QuizDAO();
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

            response.setStatus(401);

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Not logged in\"}"
            );

            return;
        }

        String role =
                (String) session.getAttribute("role");

        if (!"STUDENT".equals(role)) {

            response.setStatus(403);

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Student access required\"}"
            );

            return;
        }

        int studentId =
                (Integer) session.getAttribute("userId");

        System.out.println(
                "Loading quizzes for student ID: "
                + studentId
        );

        List<Quiz> quizzes =
                quizDAO.getStudentQuizzes(studentId);

        System.out.println(
                "Quizzes found: "
                + quizzes.size()
        );

        StringBuilder json =
                new StringBuilder();

        json.append(
                "{\"success\":true,\"quizzes\":["
        );

        for (int i = 0;
             i < quizzes.size();
             i++) {

            Quiz quiz = quizzes.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{");

            json.append("\"id\":")
                    .append(quiz.getId())
                    .append(",");

            json.append("\"subjectId\":")
                    .append(quiz.getSubjectId())
                    .append(",");

            json.append("\"subjectName\":\"")
                    .append(escape(quiz.getSubjectName()))
                    .append("\",");

            json.append("\"topic\":\"")
                    .append(escape(quiz.getTopic()))
                    .append("\",");

            json.append("\"quizDate\":\"")
                    .append(quiz.getQuizDate())
                    .append("\",");

            json.append("\"quizTime\":\"")
                    .append(quiz.getQuizTime())
                    .append("\",");

            json.append("\"totalQuestions\":")
                    .append(quiz.getTotalQuestions());

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