package co.ianh.kstool_jetty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by henderson_i on 4/24/16.
 */
public class CheckSession {

    public static boolean check(HttpServletRequest req, HttpServletResponse resp) {
        // Get session
        HttpSession session = req.getSession();

        // If session does not exist, return 403
        if ( session.getAttribute("id") == null ) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try {
                resp.getWriter().println("User not authorized.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        return true;

    }
}
