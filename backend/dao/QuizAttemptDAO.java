package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QuizAttemptDAO {

    public static class Attempt {

        private int id;
        private int studentId;
        private int quizId;
        private int currentQuestion;
        private int score;
        private String questionStartedAt;
        private String status;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getStudentId() {
            return studentId;
        }

        public void setStudentId(int studentId) {
            this.studentId = studentId;
        }

        public int getQuizId() {
            return quizId;
        }

        public void setQuizId(int quizId) {
            this.quizId = quizId;
        }

        public int getCurrentQuestion() {
            return currentQuestion;
        }

        public void setCurrentQuestion(int currentQuestion) {
            this.currentQuestion = currentQuestion;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getQuestionStartedAt() {
            return questionStartedAt;
        }

        public void setQuestionStartedAt(String questionStartedAt) {
            this.questionStartedAt = questionStartedAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }


    // ==========================================
    // GET ATTEMPT
    // ==========================================

    public Attempt getAttempt(int studentId, int quizId) {

        String sql = """
            SELECT
                id,
                student_id,
                quiz_id,
                current_question,
                score,
                question_started_at,
                status
            FROM quiz_attempts
            WHERE student_id = ?
              AND quiz_id = ?
            """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, studentId);
            statement.setInt(2, quizId);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {

                Attempt attempt = new Attempt();

                attempt.setId(rs.getInt("id"));
                attempt.setStudentId(rs.getInt("student_id"));
                attempt.setQuizId(rs.getInt("quiz_id"));

                attempt.setCurrentQuestion(
                        rs.getInt("current_question")
                );

                attempt.setScore(
                        rs.getInt("score")
                );

                attempt.setQuestionStartedAt(
                        rs.getString("question_started_at")
                );

                attempt.setStatus(
                        rs.getString("status")
                );

                return attempt;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // ==========================================
    // CREATE ATTEMPT
    // ==========================================

    public Attempt createAttempt(
            int studentId,
            int quizId
    ) {

        String sql = """
            INSERT INTO quiz_attempts
            (
                student_id,
                quiz_id,
                current_question,
                score,
                question_started_at,
                status
            )
            VALUES
            (?, ?, 0, 0, NOW(), 'IN_PROGRESS')
            """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, studentId);
            statement.setInt(2, quizId);

            statement.executeUpdate();

            return getAttempt(studentId, quizId);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    // ==========================================
    // GET OR CREATE ATTEMPT
    // ==========================================

    public Attempt getOrCreateAttempt(
            int studentId,
            int quizId
    ) {

        Attempt attempt =
                getAttempt(studentId, quizId);

        if (attempt != null) {
            return attempt;
        }

        return createAttempt(
                studentId,
                quizId
        );
    }


    // ==========================================
    // UPDATE ATTEMPT
    // ==========================================

    public boolean updateAttempt(
            int studentId,
            int quizId,
            int currentQuestion,
            int score
    ) {

        String sql = """
            UPDATE quiz_attempts
            SET
                current_question = ?,
                score = ?,
                question_started_at = NOW()
            WHERE student_id = ?
              AND quiz_id = ?
              AND status = 'IN_PROGRESS'
            """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, currentQuestion);
            statement.setInt(2, score);
            statement.setInt(3, studentId);
            statement.setInt(4, quizId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    // ==========================================
    // COMPLETE ATTEMPT
    // ==========================================

    public boolean completeAttempt(
            int studentId,
            int quizId,
            int score
    ) {

        String sql = """
            UPDATE quiz_attempts
            SET
                score = ?,
                completed_at = NOW(),
                status = 'COMPLETED'
            WHERE student_id = ?
              AND quiz_id = ?
              AND status = 'IN_PROGRESS'
            """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, score);
            statement.setInt(2, studentId);
            statement.setInt(3, quizId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}