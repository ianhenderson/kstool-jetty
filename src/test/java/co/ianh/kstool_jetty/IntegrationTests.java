package co.ianh.kstool_jetty;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.junit.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by henderson_i on 4/12/16.
 */
public class IntegrationTests {

    static CloseableHttpClient client;
    static Server app;
    String HOSTNAME = "localhost";
    int PORT = 8000;

    // Start up server
    @BeforeClass
    public static void setUp() throws Exception {
        app = App.makeServer();
        client = HttpClients.createDefault();
    }

    // Shut down server
    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("All tore down, brah.");
        app.stop();
    }

    /* Helper methods */

    private URI buildURI(String url) throws URISyntaxException {
        return new URIBuilder()
                .setScheme("http")
                .setHost(HOSTNAME)
                .setPort(PORT)
                .setPath(url)
                .build();
    }

    private HttpResponse doGET(String url) throws IOException, URISyntaxException {
        URI uri = buildURI(url);
        HttpGet get = new HttpGet(uri);
        return client.execute(get);
    }

    private HttpResponse doPOST(String url) throws IOException, URISyntaxException {
        URI uri = buildURI(url);
        HttpPost post = new HttpPost(uri);
        return client.execute(post);
    }

    /* Integration tests */

    @Test
    public void getFacts() throws Exception {
        int status = doGET("/facts").getStatusLine().getStatusCode();
        Assert.assertEquals(200, status);
    }

    @Test
    public void postLogin() throws Exception {
        int status = doPOST("/login").getStatusLine().getStatusCode();
        Assert.assertEquals(200, status);
    }

}