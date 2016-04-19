package co.ianh.kstool_jetty;

/**
 * Created by henderson_i on 4/18/16.
 */
public class Config {
    static String ENV;
    static String filename;
    static int port;

    static {
        ENV = System.getProperty("FACTLY");
        if (Utils.isEmpty(ENV)) {
            ENV = "prod";
        }
        setConfigs(ENV);
        System.out.println("ENV: " + ENV);
        System.out.println("filename: " + filename);
        System.out.println("port: " + port);
    }

    private static void setConfigs(String env) {
        switch (env) {
            case "prod":
                filename = "test.db";
                port = 3000;
                break;
            case "test":
                filename = ""; // use in-memory db file
                port = 3001;
                break;
        }
    }

}
