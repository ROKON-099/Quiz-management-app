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

        System.out.println(
                "TeacherCourseServlet initialized"
        );
    }


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {


        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );


        /*
         * ==========================================
         * GET SESSION
         * ==========================================
         */

        HttpSession session =
                request.getSession(false);


        if (session == null ||
            session.getAttribute("userId") == null) {


            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );


            response.getWriter().write(
                    """
                    {
                        "success": false,
                        "message": "Not logged in"
                    }
                    """
            );

            return;
        }


        /*
         * ==========================================
         * GET DATABASE USER ID
         * ==========================================
         */

        Object userIdObject =
                session.getAttribute("userId");


        if (!(userIdObject instanceof Integer)) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().write(
                    """
                    {
                        "success": false,
                        "message": "Invalid session"
                    }
                    """
            );

            return;
        }


        int teacherId =
                (Integer) userIdObject;


        /*
         * ==========================================
         * CHECK ROLE
         * ==========================================
         */

        String role =
                (String) session.getAttribute("role");


        if (!"TEACHER".equals(role)) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            response.getWriter().write(
                    """
                    {
                        "success": false,
                        "message": "Teacher access required"
                    }
                    """
            );

            return;
        }


        /*
         * ==========================================
         * GET TEACHER COURSES
         * ==========================================
         */

        List<Course> courses =
                courseDAO.getTeacherCourses(
                        teacherId
                );


        /*
         * DEBUG
         */

        System.out.println(
                "Teacher database ID: "
                + teacherId
        );

        System.out.println(
                "Courses found: "
                + courses.size()
        );


        /*
         * ==========================================
         * JSON
         * ==========================================
         */

        StringBuilder json =
                new StringBuilder();


        json.append(
                "{\"success\":true,\"courses\":["
        );


        for (int i = 0;
             i < courses.size();
             i++) {


            Course course =
                    courses.get(i);


            if (i > 0) {
                json.append(",");
            }


            json.append("{");


            json.append(
                    "\"id\":"
            )
            .append(
                    course.getId()
            )
            .append(",");


            json.append(
                    "\"name\":\""
            )
            .append(
                    escapeJson(
                            course.getCourseTitle()
                    )
            )
            .append("\"");


            json.append("}");
        }


        json.append("]}");


        response.getWriter().write(
                json.toString()
        );
    }


    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}