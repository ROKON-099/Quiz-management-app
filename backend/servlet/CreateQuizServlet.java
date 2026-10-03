package servlet;

import dao.QuizDAO;
import model.Question;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/create-quiz")
public class CreateQuizServlet extends HttpServlet {

    private QuizDAO quizDAO;

    @Override
    public void init() throws ServletException {
        quizDAO = new QuizDAO();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            // ==============================
            // SESSION
            // ==============================

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

            String role =
                    (String) session.getAttribute("role");

            if (!"TEACHER".equals(role)) {

                response.setStatus(
                        HttpServletResponse.SC_FORBIDDEN
                );

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Teacher access required\"}"
                );

                return;
            }

            int teacherId =
                    (Integer) session.getAttribute("userId");


            // ==============================
            // BASIC INFORMATION
            // ==============================

            String subjectIdText =
                    request.getParameter("subjectId");

            String topic =
                    request.getParameter("topic");

            String quizDate =
                    request.getParameter("quizDate");

            String quizTime =
                    request.getParameter("quizTime");


            if (subjectIdText == null ||
                topic == null ||
                quizDate == null ||
                quizTime == null ||
                subjectIdText.trim().isEmpty() ||
                topic.trim().isEmpty() ||
                quizDate.trim().isEmpty() ||
                quizTime.trim().isEmpty()) {

                response.setStatus(400);

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Course, topic, date and time are required\"}"
                );

                return;
            }


            int subjectId;

            try {

                subjectId =
                        Integer.parseInt(
                                subjectIdText
                        );

            } catch (NumberFormatException e) {

                response.setStatus(400);

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Invalid course ID\"}"
                );

                return;
            }


            // ==============================
            // CHECK TEACHER COURSE
            // ==============================

            boolean allowed =
                    quizDAO.isTeacherCourse(
                            subjectId,
                            teacherId
                    );

            if (!allowed) {

                response.setStatus(
                        HttpServletResponse.SC_FORBIDDEN
                );

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"This course is not assigned to you\"}"
                );

                return;
            }


            // ==============================
            // GET 10 QUESTIONS
            // ==============================

            List<Question> questions =
                    new ArrayList<>();


            for (int i = 1; i <= 10; i++) {

                String questionText =
                        request.getParameter(
                                "question_" + i
                        );

                String optionA =
                        request.getParameter(
                                "option_a_" + i
                        );

                String optionB =
                        request.getParameter(
                                "option_b_" + i
                        );

                String optionC =
                        request.getParameter(
                                "option_c_" + i
                        );

                String optionD =
                        request.getParameter(
                                "option_d_" + i
                        );

                String correctAnswer =
                        request.getParameter(
                                "correct_answer_" + i
                        );


                if (questionText == null ||
                    optionA == null ||
                    optionB == null ||
                    optionC == null ||
                    optionD == null ||
                    correctAnswer == null ||

                    questionText.trim().isEmpty() ||
                    optionA.trim().isEmpty() ||
                    optionB.trim().isEmpty() ||
                    optionC.trim().isEmpty() ||
                    optionD.trim().isEmpty() ||

                    !correctAnswer.matches("[ABCD]")) {

                    response.setStatus(400);

                    response.getWriter().write(
                            "{\"success\":false,\"message\":\"Question "
                            + i
                            + " is incomplete\"}"
                    );

                    return;
                }


                Question question =
                        new Question();

                question.setQuestion(
                        questionText.trim()
                );

                question.setOptionA(
                        optionA.trim()
                );

                question.setOptionB(
                        optionB.trim()
                );

                question.setOptionC(
                        optionC.trim()
                );

                question.setOptionD(
                        optionD.trim()
                );

                question.setCorrectAnswer(
                        correctAnswer
                );

                questions.add(question);
            }


            // ==============================
            // CREATE QUIZ
            // ==============================

            int quizId =
                    quizDAO.createQuiz(
                            subjectId,
                            topic.trim(),
                            quizDate,
                            quizTime,
                            teacherId,
                            questions
                    );


            if (quizId <= 0) {

                response.setStatus(500);

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Database failed to create quiz\"}"
                );

                return;
            }


            // ==============================
            // SUCCESS
            // ==============================

            response.setStatus(200);

            response.getWriter().write(
                    "{"
                    + "\"success\":true,"
                    + "\"message\":\"Quiz created successfully\","
                    + "\"quizId\":" + quizId
                    + "}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(500);

            response.getWriter().write(
                    "{"
                    + "\"success\":false,"
                    + "\"message\":\""
                    + escapeJson(
                            e.getMessage()
                    )
                    + "\""
                    + "}"
            );
        }
    }


    private String escapeJson(String value) {

        if (value == null) {
            return "Unknown server error";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}