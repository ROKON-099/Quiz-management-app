package servlet;

import dao.QuizAttemptDAO;
import dao.QuizDAO;
import model.Question;
import model.Quiz;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/quiz")
public class QuizServlet extends HttpServlet {

    private QuizDAO quizDAO;
    private QuizAttemptDAO attemptDAO;

    @Override
    public void init() throws ServletException {

        quizDAO = new QuizDAO();
        attemptDAO = new QuizAttemptDAO();

        System.out.println("QuizServlet initialized");
    }


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        // ==========================================
        // SESSION CHECK
        // ==========================================

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


        // ==========================================
        // ROLE CHECK
        // ==========================================

        String role =
                (String) session.getAttribute("role");

        if (!"STUDENT".equals(role)) {

            response.setStatus(403);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Student access required\"}"
            );

            return;
        }


        // ==========================================
        // QUIZ ID
        // ==========================================

        String quizIdParameter =
                request.getParameter("id");

        if (quizIdParameter == null ||
            quizIdParameter.trim().isEmpty()) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Quiz ID is missing\"}"
            );

            return;
        }


        int quizId;

        try {

            quizId =
                    Integer.parseInt(
                        quizIdParameter
                    );

        } catch (NumberFormatException e) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid Quiz ID\"}"
            );

            return;
        }


        // ==========================================
        // STUDENT ID
        // ==========================================

        int studentId =
                (Integer) session.getAttribute(
                    "userId"
                );


        System.out.println(
            "Loading Quiz: quizId="
            + quizId
            + ", studentId="
            + studentId
        );


        // ==========================================
        // LOAD QUIZ
        // ==========================================

        Quiz quiz =
                quizDAO.getQuizForStudent(
                    quizId,
                    studentId
                );


        if (quiz == null) {

            System.out.println(
                "Quiz not found or student has no access."
            );

            response.setStatus(404);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Quiz not found or this quiz is not available for your batch/course\"}"
            );

            return;
        }


        // ==========================================
        // LOAD QUESTIONS
        // ==========================================

        List<Question> questions =
                quizDAO.getQuizQuestions(
                    quizId
                );


        if (questions == null ||
            questions.size() != 10) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"This quiz must contain exactly 10 questions\"}"
            );

            return;
        }


        // ==========================================
        // CREATE / GET ATTEMPT
        // ==========================================

        QuizAttemptDAO.Attempt attempt =
                attemptDAO.getOrCreateAttempt(
                    studentId,
                    quizId
                );


        if (attempt == null) {

            response.setStatus(500);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Could not start quiz. Check quiz_attempts table.\"}"
            );

            return;
        }


        // ==========================================
        // ALREADY COMPLETED
        // ==========================================

        if ("COMPLETED".equals(
                attempt.getStatus())) {

            response.getWriter().write(
                "{"
                + "\"success\":false,"
                + "\"completed\":true,"
                + "\"message\":\"You already completed this quiz\""
                + "}"
            );

            return;
        }


        // ==========================================
        // BUILD JSON
        // ==========================================

        StringBuilder json =
                new StringBuilder();


        json.append("{");

        json.append("\"success\":true,");


        // ==========================================
        // QUIZ INFORMATION
        // ==========================================

        json.append("\"quiz\":{");

        json.append("\"id\":")
                .append(quiz.getId())
                .append(",");

        json.append("\"subjectName\":\"")
                .append(
                    escape(
                        quiz.getSubjectName()
                    )
                )
                .append("\",");

        json.append("\"topic\":\"")
                .append(
                    escape(
                        quiz.getTopic()
                    )
                )
                .append("\",");

        json.append("\"quizDate\":\"")
                .append(
                    escape(
                        quiz.getQuizDate()
                    )
                )
                .append("\",");

        json.append("\"quizTime\":\"")
                .append(
                    escape(
                        quiz.getQuizTime()
                    )
                )
                .append("\"");

        json.append("},");


        // ==========================================
        // ATTEMPT DATA
        // ==========================================

        json.append("\"currentQuestion\":")
                .append(
                    attempt.getCurrentQuestion()
                )
                .append(",");

        json.append("\"score\":")
                .append(
                    attempt.getScore()
                )
                .append(",");


        // ==========================================
        // QUESTIONS
        // ==========================================

        json.append("\"questions\":[");


        for (int i = 0;
             i < questions.size();
             i++) {

            Question question =
                    questions.get(i);


            if (i > 0) {
                json.append(",");
            }


            json.append("{");

            json.append("\"id\":")
                    .append(
                        question.getId()
                    )
                    .append(",");

            json.append("\"question\":\"")
                    .append(
                        escape(
                            question.getQuestion()
                        )
                    )
                    .append("\",");

            json.append("\"optionA\":\"")
                    .append(
                        escape(
                            question.getOptionA()
                        )
                    )
                    .append("\",");

            json.append("\"optionB\":\"")
                    .append(
                        escape(
                            question.getOptionB()
                        )
                    )
                    .append("\",");

            json.append("\"optionC\":\"")
                    .append(
                        escape(
                            question.getOptionC()
                        )
                    )
                    .append("\",");

            json.append("\"optionD\":\"")
                    .append(
                        escape(
                            question.getOptionD()
                        )
                    )
                    .append("\"");

            json.append("}");
        }


        json.append("]");

        json.append("}");


        // ==========================================
        // SEND RESPONSE
        // ==========================================

        response.getWriter()
                .write(
                    json.toString()
                );
    }


    // ==========================================
    // JSON ESCAPE
    // ==========================================

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