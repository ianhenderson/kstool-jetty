package co.ianh.kstool_jetty;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Collectors;

/**
 * Created by henderson_i on 4/11/16.
 */
public class FactsServlet extends HttpServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1) Check for session and return if none
        boolean isAuthorized = CheckSession.check(req, resp);
        if (!isAuthorized) {
            return;
        }

        // 2) Fetch words for this user
        // TODO

        String method = req.getMethod();
        String uri = req.getRequestURI();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println(method + ": " + uri);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1) Check for session and return if none
        boolean isAuthorized = CheckSession.check(req, resp);
        if (!isAuthorized) {
            return;
        }

        // 2) Get data from POST body (username, password)
        JsonObject bodyData = Utils.getBody(req);

        // 3) Validate request body
        if ( !bodyData.containsKey("fact") ) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("Error: POST must include a fact.");
        } else {
            JsonValue facts = bodyData.get("fact");
            HttpSession session = req.getSession();
            int userId = (int) session.getAttribute("id");
            String userName = session.getAttribute("name").toString();

            // 4) Add to database and respond with 200
            DataAccessLayer.addWords(userId, facts);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.setContentType("text/html; charset=UTF-8");
            resp.getWriter().println("Success! Fact added to " + userName + "'s collection: " + facts.toString());

        }

    }
}
