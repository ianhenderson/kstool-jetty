package co.ianh.kstool_jetty;

import org.eclipse.jetty.server.Server;
import static co.ianh.kstool_jetty.Utils.getPort;

/**
 * Created by henderson_i on 3/22/16.
 */
public class App {

    public static void main(String[] args) throws Exception {
        int port = getPort();
        Server server = new Server(port);
        server.setHandler(new Handler());
        server.start();
        server.join();
    }
}
