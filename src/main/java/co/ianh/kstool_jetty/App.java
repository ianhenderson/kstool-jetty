package co.ianh.kstool_jetty;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

import static co.ianh.kstool_jetty.Utils.getHost;
import static co.ianh.kstool_jetty.Utils.getPort;

/**
 * Created by henderson_i on 3/22/16.
 */
public class App {

    public static void main(String[] args) throws Exception {

        // List of handlers
        Handler[] handlers = {
             new co.ianh.kstool_jetty.Handler(),
             FileServer.build()
        };

        // Build handlerList
        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(handlers);

        // Add gzip to responses
        GzipHandler gzip = new GzipHandler();
        gzip.setHandler(handlerList);

        // Get environment variables
        int port = getPort();
        String host = getHost();

        // Server
        Server server = new Server();

        // HTTP Connector
        ServerConnector http = new ServerConnector(server);
        http.setPort(port);
        http.setHost(host);

        // Set connector
        server.addConnector(http);

        // Set handler
        server.setHandler(gzip);

        // Start server
        server.start();
        server.join();
    }
}
