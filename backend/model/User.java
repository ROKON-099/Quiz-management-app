package model;

public class User {

    private int id;
    private String userId;
    private String name;
    private String password;
    private String role;
    private String department;
    private String batch;

    public User() {
    }

    public User(int id, String userId, String name, String password,
                String role, String department, String batch) {

        this.id = id;
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.role = role;
        this.department = department;
        this.batch = batch;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }
}