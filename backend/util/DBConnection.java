package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mariadb://localhost:3306/quizrank";

    private static final String USER = "root";

    private static final String PASSWORD = "";

    public static Connection getConnection() {

        try {
            Class.forName("org.mariadb.jdbc.Driver");

            Connection connection = DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

            System.out.println("================================");
            System.out.println("MariaDB Connected Successfully!");
            System.out.println("Database: quizrank");
            System.out.println("================================");

            return connection;

        } catch (ClassNotFoundException e) {

            System.out.println("MariaDB Driver Not Found!");
            e.printStackTrace();

        } catch (SQLException e) {

            System.out.println("MariaDB Connection Failed!");
            e.printStackTrace();
        }

        return null;
    }
}