package co.ianh.kstool_jetty;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

import static co.ianh.kstool_jetty.Utils.getPort;

/**
 * Created by henderson_i on 3/22/16.
 */
public class App {

    public static void main(String[] args) throws Exception {
        HandlerList handlers = new HandlerList();
//        handlers.setHandlers(new Handler[] { new co.ianh.kstool_jetty.Handler(), new FileServer().build()});
        handlers.setHandlers(new Handler[] { new FileServer().build()});

        GzipHandler gzip = new GzipHandler();
        gzip.setHandler(handlers);

        int port = getPort();
        Server server = new Server(port);
        server.setHandler(gzip);


//        server.setHandler(new Handler());
        server.start();
        server.join();
    }
}
