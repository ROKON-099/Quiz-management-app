
package util;

import java.sql.Connection;

public class TestConnection {

    public static void main(String[] args) {

        Connection connection =
                DBConnection.getConnection();

        if (connection != null) {

            System.out.println(
                    "QuizRank Database Connection Successful!"
            );

        } else {

            System.out.println(
                    "QuizRank Database Connection Failed!"
            );
        }
    }
}