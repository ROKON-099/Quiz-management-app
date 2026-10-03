package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AcademicRecordDAO {

    public ResultSet getAcademicRecord(int studentId) {

        return null;
    }

    public double getCurrentCgpa(int studentId) {

        String sql = """
            SELECT cgpa
            FROM academic_records
            WHERE student_id = ?
            AND semester = 'Current'
            LIMIT 1
            """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, studentId);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getDouble("cgpa");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }


    public double getPreviousCgpa(int studentId) {

        String sql = """
            SELECT cgpa
            FROM academic_records
            WHERE student_id = ?
            AND semester = 'Previous'
            LIMIT 1
            """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, studentId);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getDouble("cgpa");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }


    public int getBatchRank(int studentId) {

        String sql = """
            SELECT batch_rank
            FROM academic_records
            WHERE student_id = ?
            AND semester = 'Current'
            LIMIT 1
            """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, studentId);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getInt("batch_rank");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }


    public int getDepartmentRank(int studentId) {

        String sql = """
            SELECT department_rank
            FROM academic_records
            WHERE student_id = ?
            AND semester = 'Current'
            LIMIT 1
            """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, studentId);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getInt("department_rank");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}