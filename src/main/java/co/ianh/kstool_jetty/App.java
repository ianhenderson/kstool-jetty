package co.ianh.kstool_jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import static co.ianh.kstool_jetty.Utils.getHost;
import static co.ianh.kstool_jetty.Utils.getPort;

/**
 * Created by henderson_i on 3/22/16.
 */
public class App {

    public static void main(String[] args) throws Exception {
        Server server = makeServer();
        server.join();
    }

    public static Server makeServer() throws Exception {
        // Instantiate server
        int port = getPort();
        Server server = new Server(port);

        // Build servlet handler
        Handler servletHandler = buildServletHandler();

        // Set handler
        server.setHandler(servletHandler);

        // Start server
        server.start();
        return server;
    }

    private static ServerConnector configureConnector(Server server) throws Exception {
        // Get environment variables
        int port = getPort();
        String host = getHost();

        // HTTP Connector
        ServerConnector http = new ServerConnector(server);
        http.setPort(port);
        http.setHost(host);

        // Set connector
        server.addConnector(http);

        return http; // TODO: is this even necessary?
    }

    private static Handler buildServletHandler() throws Exception {
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);

        // Set base directory for static content
        handler.setResourceBase("./kstool-client/public/angular-mat-design");

        // Routes
        handler.addServlet(KanjiServlet.class, "/api/kanji");
        handler.addServlet(FactsServlet.class, "/api/facts");
        handler.addServlet(SignupServlet.class, "/api/signup");
        handler.addServlet(LoginServlet.class, "/api/login");
        handler.addServlet(LogoutServlet.class, "/api/logout");
        handler.addServlet(DefaultServlet.class, "/");

        return handler;
    }

}
