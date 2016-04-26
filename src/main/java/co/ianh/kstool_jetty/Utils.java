/**
 * Created by henderson_i on 3/23/16.
 */
package co.ianh.kstool_jetty;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Collectors;

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
                        8000;
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

    public static JsonObject getBody(HttpServletRequest req) {
        // 1) Get data from POST body (username, password)
        String body = null;
        try {
            body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2) Parse string into JSON object
        JsonReader jsonReader = Json.createReader(new StringReader(body));
        JsonObject bodyData = jsonReader.readObject();
        jsonReader.close();

        return  bodyData;
    }

    public static String filterKanji(String s) {
        return s.replaceAll("[^\\u4e00-\\u9faf]", "");
    }
}
