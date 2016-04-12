package co.ianh.kstool_jetty;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by henderson_i on 4/11/16.
 */
public class LogoutServlet extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        String uri = req.getRequestURI();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(method + ": " + uri);
    }
}
