package co.ianh.kstool_jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletHandler;

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

        // Build context
        Handler servletHandler = buildServletHandler();

        // Set handler
        server.setHandler(servletHandler);

        // Start server
        server.start();
        return server;
    }

    private static Handler buildContext() throws Exception {
        // Build up contexts to be handled
        ContextHandlerCollection contexts = buildContexts();

        // Add gzip to responses
        GzipHandler gzip = new GzipHandler();
        gzip.setHandler(contexts);

        return gzip;
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

    private static ContextHandlerCollection buildContexts() throws Exception {
        // Hello handler
        Handler hello = new co.ianh.kstool_jetty.Handler();

        // Fileserver handler
        ResourceHandler fs = FileServer.build();

        // Create contextHandler for /
        ContextHandler helloContext = new ContextHandler();
        helloContext.setContextPath("/");
        helloContext.setHandler(hello);

        // Create contextHandler for /files
        ContextHandler fileContext = new ContextHandler();
        fileContext.setContextPath("/files");
        fileContext.setHandler(fs);

        Handler[] handlers = {
                helloContext,
                fileContext
        };

        // Create collection of contexts
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(handlers);

        return contexts;
    }

    private static Handler buildServletHandler() throws Exception {
        ServletHandler handler = new ServletHandler();

        // Routes
        handler.addServletWithMapping(KanjiServlet.class, "/kanji");
        handler.addServletWithMapping(FactsServlet.class, "/facts");
        handler.addServletWithMapping(SignupServlet.class, "/signup");
        handler.addServletWithMapping(LoginServlet.class, "/login");
        handler.addServletWithMapping(LogoutServlet.class, "/logout");

        return handler;
    }

}
