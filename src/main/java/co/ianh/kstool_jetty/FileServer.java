package co.ianh.kstool_jetty;

import org.eclipse.jetty.server.handler.ResourceHandler;

/**
 * Created by henderson_i on 3/23/16.
 */
public class FileServer {

    public static ResourceHandler build() throws Exception {
        ResourceHandler rh = new ResourceHandler();
        rh.setDirectoriesListed(true);
        rh.setWelcomeFiles(new String[] { "index.html"});
        rh.setResourceBase(".");
        return rh;
    }



}
