package co.ianh.kstool_jetty;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import co.ianh.kstool_jetty.Utils;

/**
 * Created by henderson_i on 3/22/16.
 */
public class App extends AbstractHandler {

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("Sup, beautiful world.");
        baseRequest.setHandled(true);
    }

    public static void main(String[] args) throws Exception {
        int port = Utils.getPort();
        Server server = new Server(port);
        server.setHandler(new App());
        server.start();
        server.join();
    }
}
