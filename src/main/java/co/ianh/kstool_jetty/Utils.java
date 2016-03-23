/**
 * Created by henderson_i on 3/23/16.
 */
package co.ianh.kstool_jetty;

public class Utils {

    public static boolean isEmpty(String s) {
        return (s == null || s.isEmpty());
    }

    public static int getPort() {
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

    public static String getHost() {
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
}
