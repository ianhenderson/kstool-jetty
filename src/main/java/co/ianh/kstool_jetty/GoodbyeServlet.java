package co.ianh.kstool_jetty;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by henderson_i on 4/4/16.
 */
public class GoodbyeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text");
        resp.setStatus(HttpServletResponse.SC_OK);
        DataAccessLayer.initTables();
        resp.getWriter().println("You even code, bruh?");
    }
}
