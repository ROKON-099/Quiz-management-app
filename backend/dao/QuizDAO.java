package dao;

import util.DBConnection;

import java.sql.*;

public class QuizAttemptDAO {


    public int startAttempt(
            int studentId,
            int quizId) {

        String checkSql = """
            SELECT id, status
            FROM quiz_attempts
            WHERE student_id = ?
            AND quiz_id = ?
            """;


        String insertSql = """
            INSERT INTO quiz_attempts
            (
                student_id,
                quiz_id,
                current_question,
                score,
                question_started_at
            )
            VALUES (?, ?, 0, 0, NOW())
            """;


        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement check =
                    connection.prepareStatement(
                            checkSql
                    )
        ) {

            check.setInt(1, studentId);
            check.setInt(2, quizId);

            ResultSet rs =
                    check.executeQuery();


            if (rs.next()) {

                return rs.getInt("id");
            }


            try (
                PreparedStatement insert =
                        connection.prepareStatement(
                                insertSql,
                                Statement.RETURN_GENERATED_KEYS
                        )
            ) {

                insert.setInt(
                        1,
                        studentId
                );

                insert.setInt(
                        2,
                        quizId
                );

                insert.executeUpdate();

                ResultSet keys =
                        insert.getGeneratedKeys();

                if (keys.next()) {

                    return keys.getInt(1);
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return -1;
    }
}