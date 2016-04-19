package co.ianh.kstool_jetty;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
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
public class LoginServlet extends HttpServlet{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        HttpSession session = req.getSession();

        // 1) Get data from POST body (username, password)
        String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        // 1.1) Parse string into JSON object
        JsonReader jsonReader = Json.createReader(new StringReader(body));
        JsonObject bodyData = jsonReader.readObject();
        jsonReader.close();

        // 2) Validate request body
        if ( !bodyData.containsKey("username") || !bodyData.containsKey("password") ) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("Error: POST must include a username and password.");
            return;
        }

        // 3) Check whether user exists, and return 409 if so
        String username = bodyData.get("username").toString();
        String password = bodyData.get("password").toString();
        String responseBody;
        int status;

        JsonObject responseBodyObject = DataAccessLayer.checkUsernameAndPassword(username, password);
        if (responseBodyObject != null) {
            responseBody = responseBodyObject.toString();
            status = HttpServletResponse.SC_OK;
        } else {
            responseBody = "Error: username / password incorrect.";
            status = HttpServletResponse.SC_FORBIDDEN;
        }

        resp.setStatus(status);
        resp.getWriter().println(responseBody);
    }
}
