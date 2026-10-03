package servlet;

import dao.CourseDAO;
import model.Course;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/teacher-courses")
public class TeacherCourseServlet extends HttpServlet {

    private CourseDAO courseDAO;

    @Override
    public void init() throws ServletException {
        courseDAO = new CourseDAO();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Not logged in\"}"
            );

            return;
        }

        int teacherId =
                (Integer) session.getAttribute("userId");

        List<Course> courses =
                courseDAO.getTeacherCourses(teacherId);

        StringBuilder json =
                new StringBuilder();

        json.append("{\"success\":true,\"courses\":[");

        for (int i = 0; i < courses.size(); i++) {

            Course course = courses.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("{");

            json.append("\"id\":")
                    .append(course.getId())
                    .append(",");

            json.append("\"name\":\"")
                    .append(escape(course.getCourseTitle()))
                    .append("\"");

            json.append("}");
        }

        json.append("]}");

        response.getWriter().write(
                json.toString()
        );
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}