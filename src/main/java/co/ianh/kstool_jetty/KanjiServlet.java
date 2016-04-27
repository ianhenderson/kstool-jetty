package co.ianh.kstool_jetty;

import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by henderson_i on 4/11/16.
 */
public class KanjiServlet extends HttpServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1) Check for session and return if none
        boolean isAuthorized = CheckSession.check(req, resp);
        if (!isAuthorized) { return; }

        // 2) Fetch next queued kanji, related words for this user
        HttpSession session = req.getSession();
        int userId = (int) session.getAttribute("id");
        String kanjiAndWords = null;
        try {
            kanjiAndWords = DataAccessLayer.getNextKanjiAndRelatedWords(userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3) If no results, return 404, else 200 w/ data
        int status = 0;
        String bodyResponse = "";
        if (kanjiAndWords == null) {
            status = 404;
            bodyResponse = "Nothing more to study.";
        } else {
            status = 200;
            bodyResponse = kanjiAndWords;
            resp.setContentType("application/json; charset=UTF-8");
        }

        resp.setStatus(status);
        resp.getWriter().println(bodyResponse);
    }
}
