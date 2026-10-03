package servlet;

import dao.QuizAttemptDAO;
import dao.QuizDAO;
import model.Question;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@WebServlet("/quiz-answer")
public class SubmitQuizServlet extends HttpServlet {

    private QuizDAO quizDAO;
    private QuizAttemptDAO attemptDAO;

    @Override
    public void init() throws ServletException {

        quizDAO = new QuizDAO();
        attemptDAO = new QuizAttemptDAO();

        System.out.println("SubmitQuizServlet initialized");
    }

    @Override
    protected void doPost(
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

        // ==========================================
        // REQUEST DATA
        // ==========================================

        String quizIdParam =
                request.getParameter("quizId");

        String questionIndexParam =
                request.getParameter("questionIndex");

        String answer =
                request.getParameter("answer");

        if (quizIdParam == null ||
            questionIndexParam == null) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Missing quiz ID or question index\"}"
            );

            return;
        }

        int quizId;
        int questionIndex;

        try {

            quizId =
                    Integer.parseInt(quizIdParam);

            questionIndex =
                    Integer.parseInt(questionIndexParam);

        } catch (NumberFormatException e) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid quiz ID or question index\"}"
            );

            return;
        }

        if (answer == null) {
            answer = "";
        }

        answer =
                answer.trim().toUpperCase();

        System.out.println(
            "Submitting answer: student="
            + studentId
            + ", quiz="
            + quizId
            + ", question="
            + questionIndex
            + ", answer="
            + answer
        );

        // ==========================================
        // GET ATTEMPT
        // ==========================================

        QuizAttemptDAO.Attempt attempt =
                attemptDAO.getAttempt(
                        studentId,
                        quizId
                );

        if (attempt == null) {

            response.setStatus(404);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Quiz attempt not found\"}"
            );

            return;
        }

        // ==========================================
        // CHECK COMPLETED
        // ==========================================

        if ("COMPLETED".equals(
                attempt.getStatus())) {

            response.getWriter().write(
                "{\"success\":false,\"completed\":true,\"message\":\"Quiz already completed\"}"
            );

            return;
        }

        // ==========================================
        // CHECK QUESTION SEQUENCE
        // ==========================================

        if (questionIndex !=
                attempt.getCurrentQuestion()) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid question sequence\"}"
            );

            return;
        }

        // ==========================================
        // LOAD QUESTIONS
        // ==========================================

        List<Question> questions =
                quizDAO.getQuizQuestions(quizId);

        if (questions == null ||
            questions.size() != 10) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Quiz must contain exactly 10 questions\"}"
            );

            return;
        }

        if (questionIndex < 0 ||
            questionIndex >= questions.size()) {

            response.setStatus(400);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid question\"}"
            );

            return;
        }

        Question question =
                questions.get(questionIndex);

        // ==========================================
        // SERVER-SIDE 30 SECOND TIMER
        // ==========================================

        LocalDateTime startedAt;

        try {

            startedAt =
                    LocalDateTime.parse(
                        attempt.getQuestionStartedAt()
                            .replace(" ", "T")
                    );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(500);

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid question start time\"}"
            );

            return;
        }

        long elapsed =
                ChronoUnit.SECONDS.between(
                    startedAt,
                    LocalDateTime.now()
                );

        boolean timeExpired =
                elapsed >= 30;

        System.out.println(
            "Question started: "
            + startedAt
            + ", elapsed: "
            + elapsed
            + " seconds"
        );

        // ==========================================
        // CHECK ANSWER
        // ==========================================

        boolean correct = false;

        if (!timeExpired &&
            answer.matches("[ABCD]")) {

            String correctAnswer =
                    getCorrectAnswer(
                        question.getId()
                    );

            correct =
                    answer.equalsIgnoreCase(
                        correctAnswer
                    );
        }

        // ==========================================
        // UPDATE SCORE
        // ==========================================

        int newScore =
                attempt.getScore();

        if (correct) {
            newScore++;
        }

        // ==========================================
        // CHECK LAST QUESTION
        // ==========================================

        boolean lastQuestion =
                questionIndex ==
                questions.size() - 1;

        // ==========================================
        // QUIZ COMPLETED
        // ==========================================

        if (lastQuestion) {

            attemptDAO.completeAttempt(
                    studentId,
                    quizId,
                    newScore
            );

            saveResult(
                    studentId,
                    quizId,
                    newScore,
                    questions.size()
            );

            response.getWriter().write(
                "{"
                + "\"success\":true,"
                + "\"completed\":true,"
                + "\"score\":" + newScore + ","
                + "\"total\":" + questions.size() + ","
                + "\"timeExpired\":" + timeExpired
                + "}"
            );

            return;
        }

        // ==========================================
        // MOVE TO NEXT QUESTION
        // ==========================================

        int nextQuestion =
                questionIndex + 1;

        attemptDAO.updateAttempt(
                studentId,
                quizId,
                nextQuestion,
                newScore
        );

        // ==========================================
        // RESPONSE
        // ==========================================

        response.getWriter().write(
            "{"
            + "\"success\":true,"
            + "\"completed\":false,"
            + "\"nextQuestion\":" + nextQuestion + ","
            + "\"score\":" + newScore + ","
            + "\"correct\":" + correct + ","
            + "\"timeExpired\":" + timeExpired
            + "}"
        );
    }

    // ==========================================
    // GET CORRECT ANSWER
    // ==========================================

    private String getCorrectAnswer(
            int questionId) {

        String sql = """
            SELECT correct_answer
            FROM questions
            WHERE id = ?
            """;

        try (
            Connection connection =
                    util.DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, questionId);

            try (ResultSet rs =
                    statement.executeQuery()) {

                if (rs.next()) {

                    return rs.getString(
                        "correct_answer"
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return "";
    }

    // ==========================================
    // SAVE RESULT
    // ==========================================

    private void saveResult(
            int studentId,
            int quizId,
            int score,
            int total) {

        String checkSql = """
            SELECT id
            FROM quiz_results
            WHERE student_id = ?
              AND quiz_id = ?
            """;

        String insertSql = """
            INSERT INTO quiz_results
            (
                student_id,
                quiz_id,
                score,
                total
            )
            VALUES (?, ?, ?, ?)
            """;

        try (
            Connection connection =
                    util.DBConnection.getConnection();

            PreparedStatement check =
                    connection.prepareStatement(
                        checkSql
                    )
        ) {

            check.setInt(1, studentId);
            check.setInt(2, quizId);

            try (ResultSet rs =
                    check.executeQuery()) {

                if (rs.next()) {
                    return;
                }
            }

            try (
                PreparedStatement insert =
                        connection.prepareStatement(
                            insertSql
                        )
            ) {

                insert.setInt(1, studentId);
                insert.setInt(2, quizId);
                insert.setInt(3, score);
                insert.setInt(4, total);

                insert.executeUpdate();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}