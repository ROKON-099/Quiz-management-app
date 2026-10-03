import dao.UserDAO;
import model.User;

public class TestUserDAO {

    public static void main(String[] args) {

        UserDAO userDAO = new UserDAO();

        User user = userDAO.login("CSE001", "123456");

        if (user != null) {

            System.out.println("Login Successful!");
            System.out.println("User ID: " + user.getUserId());
            System.out.println("Name: " + user.getName());
            System.out.println("Role: " + user.getRole());
            System.out.println("Department: " + user.getDepartment());
            System.out.println("Batch: " + user.getBatch());

        } else {

            System.out.println("Login Failed!");
        }
    }
}