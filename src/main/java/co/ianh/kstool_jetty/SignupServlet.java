package co.ianh.kstool_jetty;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * Created by henderson_i on 4/11/16.
 */
public class SignupServlet extends HttpServlet{
    JsonBuilderFactory factory = Json.createBuilderFactory(null);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

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
        boolean userAlreadyExists = false;
        try {
            userAlreadyExists = DataAccessLayer.checkUserExists(username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (userAlreadyExists) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().println("Error: User already in database.");
            return;
        }

        // 4) Otherwise, add to DB
        int userAdded = 0;
        try {
            userAdded = DataAccessLayer.addUser(username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().println(userAdded);
    }
}
