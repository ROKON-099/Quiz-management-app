package dao;

import model.Notice;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    // ==============================
    // CREATE NOTICE
    // ==============================

    public boolean createNotice(
            int teacherId,
            String topic,
            String message) {

        String sql = """
            INSERT INTO notices
            (teacher_id, subject_id, topic, message, quiz_date, quiz_time)
            VALUES (?, NULL, ?, ?, NULL, NULL)
            """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, teacherId);
            statement.setString(2, topic);
            statement.setString(3, message);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    // ==============================
    // GET ALL NOTICES
    // ==============================

    public List<Notice> getAllNotices() {

        List<Notice> notices =
                new ArrayList<>();

        String sql = """
            SELECT
                id,
                teacher_id,
                topic,
                message,
                quiz_date,
                quiz_time
            FROM notices
            ORDER BY created_at DESC
            """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet rs =
                    statement.executeQuery()
        ) {

            while (rs.next()) {

                Notice notice = new Notice();

                notice.setId(
                        rs.getInt("id")
                );

                notice.setTeacherId(
                        rs.getInt("teacher_id")
                );

                notice.setTopic(
                        rs.getString("topic")
                );

                notice.setMessage(
                        rs.getString("message")
                );


                // NULL safe date

                if (rs.getDate("quiz_date") != null) {

                    notice.setQuizDate(
                            rs.getDate("quiz_date").toString()
                    );

                } else {

                    notice.setQuizDate("");

                }


                // NULL safe time

                if (rs.getTime("quiz_time") != null) {

                    notice.setQuizTime(
                            rs.getTime("quiz_time").toString()
                    );

                } else {

                    notice.setQuizTime("");

                }


                notices.add(notice);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return notices;
    }


    // ==============================
    // UPDATE NOTICE
    // ==============================

    public boolean updateNotice(
            int id,
            int teacherId,
            String topic,
            String message) {

        String sql = """
            UPDATE notices
            SET topic = ?,
                message = ?
            WHERE id = ?
            AND teacher_id = ?
            """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setString(1, topic);
            statement.setString(2, message);
            statement.setInt(3, id);
            statement.setInt(4, teacherId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    // ==============================
    // DELETE NOTICE
    // ==============================

    public boolean deleteNotice(
            int id,
            int teacherId) {

        String sql = """
            DELETE FROM notices
            WHERE id = ?
            AND teacher_id = ?
            """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);
            statement.setInt(2, teacherId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }
}