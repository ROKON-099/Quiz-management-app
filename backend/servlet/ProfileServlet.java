package servlet;

import dao.AcademicRecordDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Locale;

public class ProfileServlet extends HttpServlet {

    private AcademicRecordDAO academicRecordDAO;

    @Override
    public void init() throws ServletException {
        academicRecordDAO = new AcademicRecordDAO();
    }


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        // Get current session
        HttpSession session =
                request.getSession(false);


        // Not logged in
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


        // Database user id
        int studentId =
                (Integer) session.getAttribute("userId");


        // Get academic data
        double currentCgpa =
                academicRecordDAO.getCurrentCgpa(
                        studentId
                );

        double previousCgpa =
                academicRecordDAO.getPreviousCgpa(
                        studentId
                );

        int batchRank =
                academicRecordDAO.getBatchRank(
                        studentId
                );

        int departmentRank =
                academicRecordDAO.getDepartmentRank(
                        studentId
                );


        String json = String.format(
                Locale.US,

                """
                {
                    "success": true,
                    "academic": {
                        "currentCgpa": %.2f,
                        "previousCgpa": %.2f,
                        "batchRank": %d,
                        "departmentRank": %d
                    }
                }
                """,

                currentCgpa,
                previousCgpa,
                batchRank,
                departmentRank
        );


        response.getWriter().write(json);
    }
}