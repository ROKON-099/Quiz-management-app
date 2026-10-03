package dao;

import model.Course;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    // =====================================================
    // STUDENT COURSES
    // =====================================================

    public List<Course> getStudentCourses(int studentId) {

        List<Course> courses = new ArrayList<>();

        String sql = """
            SELECT
                s.id,
                s.name,
                sc.enrolled_type,

                COUNT(DISTINCT q.id) AS total_quizzes,

                COALESCE(
                    AVG(
                        qr.score * 100.0 /
                        NULLIF(qr.total, 0)
                    ),
                    0
                ) AS average_mark

            FROM student_courses sc

            JOIN subjects s
                ON sc.subject_id = s.id

            JOIN users u
                ON sc.student_id = u.id

            LEFT JOIN quizzes q
                ON q.subject_id = s.id

            LEFT JOIN quiz_results qr
                ON qr.quiz_id = q.id
                AND qr.student_id = sc.student_id

            WHERE sc.student_id = ?
              AND u.role = 'STUDENT'
              AND u.batch = s.batch

            GROUP BY
                s.id,
                s.name,
                sc.enrolled_type

            ORDER BY s.id
            """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, studentId);

            ResultSet rs =
                    statement.executeQuery();

            while (rs.next()) {

                Course course = new Course();

                course.setId(
                        rs.getInt("id")
                );

                course.setCourseTitle(
                        rs.getString("name")
                );

                course.setEnrolledType(
                        rs.getString("enrolled_type")
                );

                course.setTotalQuizzes(
                        rs.getInt("total_quizzes")
                );

                course.setAverageMark(
                        rs.getDouble("average_mark")
                );

                courses.add(course);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return courses;
    }


    // =====================================================
    // TEACHER COURSES
    // =====================================================

    public List<Course> getTeacherCourses(int teacherId) {

        List<Course> courses = new ArrayList<>();

        String sql = """
            SELECT
                id,
                name,
                batch
            FROM subjects
            WHERE teacher_id = ?
            ORDER BY batch, id
            """;

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, teacherId);

            ResultSet rs =
                    statement.executeQuery();

            while (rs.next()) {

                Course course = new Course();

                course.setId(
                        rs.getInt("id")
                );

                course.setCourseTitle(
                        rs.getString("name")
                );

                courses.add(course);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return courses;
    }
}