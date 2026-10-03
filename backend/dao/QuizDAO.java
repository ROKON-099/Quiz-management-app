package dao;

import model.Question;
import model.Quiz;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Time;

import java.util.ArrayList;
import java.util.List;

public class QuizDAO {

    // =====================================================
    // CHECK TEACHER COURSE
    // =====================================================

    public boolean isTeacherCourse(
            int subjectId,
            int teacherId) {

        String sql = """
            SELECT id
            FROM subjects
            WHERE id = ?
              AND teacher_id = ?
            LIMIT 1
            """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, subjectId);
            statement.setInt(2, teacherId);

            ResultSet rs =
                    statement.executeQuery();

            return rs.next();

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }


    // =====================================================
    // CREATE QUIZ
    // =====================================================

    public int createQuiz(
            int subjectId,
            String topic,
            String quizDate,
            String quizTime,
            int teacherId,
            List<Question> questions) {

        // Exactly 10 questions
        if (questions == null ||
            questions.size() != 10) {

            System.out.println(
                "Quiz must contain exactly 10 questions."
            );

            return -1;
        }

        String quizSql = """
            INSERT INTO quizzes
            (
                subject_id,
                topic,
                quiz_date,
                quiz_time,
                total_questions,
                created_by
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        String questionSql = """
            INSERT INTO questions
            (
                quiz_id,
                question,
                option_a,
                option_b,
                option_c,
                option_d,
                correct_answer
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        Connection connection = null;

        try {

            connection =
                    DBConnection.getConnection();

            if (connection == null) {

                System.out.println(
                    "Database connection is null!"
                );

                return -1;
            }

            connection.setAutoCommit(false);

            // =================================================
            // INSERT QUIZ
            // =================================================

            int quizId;

            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                            quizSql,
                            Statement.RETURN_GENERATED_KEYS
                        )
            ) {

                statement.setInt(1, subjectId);

                statement.setString(2, topic);

                statement.setDate(
                    3,
                    Date.valueOf(quizDate)
                );

                // HTML time = HH:mm
                // MySQL TIME = HH:mm:ss

                if (quizTime.length() == 5) {
                    quizTime = quizTime + ":00";
                }

                statement.setTime(
                    4,
                    Time.valueOf(quizTime)
                );

                statement.setInt(5, 10);

                statement.setInt(6, teacherId);

                statement.executeUpdate();

                ResultSet keys =
                        statement.getGeneratedKeys();

                if (!keys.next()) {

                    connection.rollback();
                    return -1;
                }

                quizId =
                        keys.getInt(1);
            }


            // =================================================
            // INSERT 10 QUESTIONS
            // =================================================

            try (
                PreparedStatement statement =
                        connection.prepareStatement(
                            questionSql
                        )
            ) {

                for (Question question : questions) {

                    statement.setInt(
                        1,
                        quizId
                    );

                    statement.setString(
                        2,
                        question.getQuestion()
                    );

                    statement.setString(
                        3,
                        question.getOptionA()
                    );

                    statement.setString(
                        4,
                        question.getOptionB()
                    );

                    statement.setString(
                        5,
                        question.getOptionC()
                    );

                    statement.setString(
                        6,
                        question.getOptionD()
                    );

                    statement.setString(
                        7,
                        question.getCorrectAnswer()
                    );

                    statement.addBatch();
                }

                statement.executeBatch();
            }


            // =================================================
            // COMMIT
            // =================================================

            connection.commit();

            System.out.println(
                "Quiz created successfully. ID = "
                + quizId
            );

            return quizId;

        } catch (Exception e) {

            e.printStackTrace();

            try {

                if (connection != null) {
                    connection.rollback();
                }

            } catch (Exception rollbackError) {
                rollbackError.printStackTrace();
            }

            return -1;

        } finally {

            try {

                if (connection != null) {

                    connection.setAutoCommit(true);
                    connection.close();
                }

            } catch (Exception closeError) {
                closeError.printStackTrace();
            }
        }
    }


    // =====================================================
    // GET STUDENT QUIZZES
    // =====================================================

    public List<Quiz> getStudentQuizzes(
            int studentId) {

        List<Quiz> quizzes =
                new ArrayList<>();

        String sql = """
            SELECT
                q.id,
                q.subject_id,
                s.name AS subject_name,
                q.topic,
                q.quiz_date,
                q.quiz_time,
                q.total_questions,
                q.created_by

            FROM quizzes q

            JOIN subjects s
                ON q.subject_id = s.id

            JOIN student_courses sc
                ON sc.subject_id = q.subject_id

            JOIN users u
                ON u.id = sc.student_id

            WHERE sc.student_id = ?
              AND u.role = 'STUDENT'
              AND u.batch = s.batch

            ORDER BY
                q.quiz_date ASC,
                q.quiz_time ASC
            """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(
                1,
                studentId
            );

            ResultSet rs =
                    statement.executeQuery();

            while (rs.next()) {

                Quiz quiz =
                        new Quiz();

                quiz.setId(
                    rs.getInt("id")
                );

                quiz.setSubjectId(
                    rs.getInt("subject_id")
                );

                quiz.setSubjectName(
                    rs.getString("subject_name")
                );

                quiz.setTopic(
                    rs.getString("topic")
                );

                Date date =
                        rs.getDate("quiz_date");

                if (date != null) {

                    quiz.setQuizDate(
                        date.toString()
                    );

                } else {

                    quiz.setQuizDate("");
                }

                Time time =
                        rs.getTime("quiz_time");

                if (time != null) {

                    quiz.setQuizTime(
                        time.toString()
                    );

                } else {

                    quiz.setQuizTime("");
                }

                quiz.setTotalQuestions(
                    rs.getInt("total_questions")
                );

                quiz.setCreatedBy(
                    rs.getInt("created_by")
                );

                quizzes.add(quiz);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return quizzes;
    }


    // =====================================================
    // GET SINGLE QUIZ FOR STUDENT
    // =====================================================

    public Quiz getQuizForStudent(
            int quizId,
            int studentId) {

        String sql = """
            SELECT
                q.id,
                q.subject_id,
                s.name AS subject_name,
                s.batch AS subject_batch,
                q.topic,
                q.quiz_date,
                q.quiz_time,
                q.total_questions,
                q.created_by

            FROM quizzes q

            JOIN subjects s
                ON q.subject_id = s.id

            JOIN student_courses sc
                ON sc.subject_id = s.id

            JOIN users u
                ON u.id = sc.student_id

            WHERE q.id = ?
              AND sc.student_id = ?
              AND u.role = 'STUDENT'
              AND u.batch = s.batch

            LIMIT 1
            """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, quizId);
            statement.setInt(2, studentId);

            ResultSet rs =
                    statement.executeQuery();

            if (rs.next()) {

                Quiz quiz =
                        new Quiz();

                quiz.setId(
                    rs.getInt("id")
                );

                quiz.setSubjectId(
                    rs.getInt("subject_id")
                );

                quiz.setSubjectName(
                    rs.getString("subject_name")
                );

                quiz.setTopic(
                    rs.getString("topic")
                );

                Date date =
                        rs.getDate("quiz_date");

                if (date != null) {

                    quiz.setQuizDate(
                        date.toString()
                    );

                } else {

                    quiz.setQuizDate("");
                }

                Time time =
                        rs.getTime("quiz_time");

                if (time != null) {

                    quiz.setQuizTime(
                        time.toString()
                    );

                } else {

                    quiz.setQuizTime("");
                }

                quiz.setTotalQuestions(
                    rs.getInt("total_questions")
                );

                quiz.setCreatedBy(
                    rs.getInt("created_by")
                );

                return quiz;
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }


    // =====================================================
    // GET QUIZ QUESTIONS
    // =====================================================

    public List<Question> getQuizQuestions(
            int quizId) {

        List<Question> questions =
                new ArrayList<>();

        String sql = """
            SELECT
                id,
                quiz_id,
                question,
                option_a,
                option_b,
                option_c,
                option_d
            FROM questions
            WHERE quiz_id = ?
            ORDER BY id ASC
            """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(
                1,
                quizId
            );

            ResultSet rs =
                    statement.executeQuery();

            while (rs.next()) {

                Question question =
                        new Question();

                question.setId(
                    rs.getInt("id")
                );

                question.setQuizId(
                    rs.getInt("quiz_id")
                );

                question.setQuestion(
                    rs.getString("question")
                );

                question.setOptionA(
                    rs.getString("option_a")
                );

                question.setOptionB(
                    rs.getString("option_b")
                );

                question.setOptionC(
                    rs.getString("option_c")
                );

                question.setOptionD(
                    rs.getString("option_d")
                );

                /*
                 * IMPORTANT:
                 * correct_answer is NOT loaded here.
                 *
                 * Student browser should never receive
                 * the correct answer.
                 */

                questions.add(question);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return questions;
    }
}