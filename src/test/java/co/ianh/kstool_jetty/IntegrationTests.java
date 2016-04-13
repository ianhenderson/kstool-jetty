package co.ianh.kstool_jetty;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.junit.*;

import java.io.IOException;

/**
 * Created by henderson_i on 4/12/16.
 */
public class IntegrationTests {

    static CloseableHttpClient client;
    static Server app;

    // Start up server
    @BeforeClass
    public static void setUp() throws Exception {
        app = App.makeServer();
        client = HttpClients.createDefault();
    }

    // Shut down server
    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("Tore down, brah.");
        app.stop();
    }

    // Helper method to make http GET requests.
    private int getStatusCode(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);
        int status = response.getStatusLine().getStatusCode();
        return status;
    }

    // Helper method to make http POST requests.
    private int postStatusCode(String url) throws IOException {
        HttpPost post = new HttpPost(url);
        HttpResponse response = client.execute(post);
        int status = response.getStatusLine().getStatusCode();
        return status;
    }

    /* Integration tests */

    @Test
    public void getFacts() throws Exception {
        int status = getStatusCode("http://localhost:8000/facts");
        Assert.assertEquals(200, status);
    }

    @Test
    public void postLogin() throws Exception {
        int status = postStatusCode("http://localhost:8000/login");
        Assert.assertEquals(200, status);
    }

}