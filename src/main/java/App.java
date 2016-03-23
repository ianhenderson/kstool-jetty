import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by henderson_i on 3/22/16.
 */
public class App extends AbstractHandler {

    private static boolean isEmpty(String s) {
        return (s == null || s.isEmpty());
    }

    private static int getPort() {
        String ENV_PORT = System.getenv("PORT");
        String SYS_PORT = System.getProperty("server.port");
        System.out.println("ENV_PORT: " + ENV_PORT);
        System.out.println("SYS_PORT: " + SYS_PORT);
        int port = (!isEmpty(SYS_PORT)) ?
                Integer.parseInt(SYS_PORT) :
                (!isEmpty(ENV_PORT)) ?
                        Integer.parseInt(ENV_PORT) :
                        8080;
        return port;
    }

    private static String getHost() {
        String ENV_HOSTNAME = System.getenv("HOSTNAME");
        String SYS_HOSTNAME = System.getProperty("server.hostname");
        System.out.println("ENV_HOSTNAME: " + ENV_HOSTNAME);
        System.out.println("SYS_HOSTNAME: " + SYS_HOSTNAME);
        String hostname = (!isEmpty(SYS_HOSTNAME)) ?
                SYS_HOSTNAME :
                (!isEmpty(ENV_HOSTNAME)) ?
                        ENV_HOSTNAME :
                        "0.0.0.0";
        return hostname;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("Sup, beautiful world.");
        baseRequest.setHandled(true);
    }

    public static void main(String[] args) throws Exception {
        int port = getPort();
        Server server = new Server(port);
        server.setHandler(new App());
        server.start();
        server.join();
    }
}
