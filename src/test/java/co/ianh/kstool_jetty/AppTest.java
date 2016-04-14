package co.ianh.kstool_jetty;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.junit.*;

import javax.json.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by henderson_i on 4/12/16.
 */
public class AppTest {

    static CloseableHttpClient client;
    static Server app;

    JsonBuilderFactory factory = Json.createBuilderFactory(null);
    JsonObject newUser1 = factory.createObjectBuilder()
            .add("username", "ian")
            .add("password", "ian123")
            .add("fact", factory.createArrayBuilder()
                    .add("日本語盛り上がりの")
            )
            .add("fact_stripped", "日本語盛上")
            .add("facts", factory.createArrayBuilder()
                    .add("名称は、")
                    .add("宇宙の膨張を発見した天文学者・エドウィン")
                    .add("ハッブルに因む。")
            )
            .add("facts_stripped", "名称宇宙膨張発見天文学者因")
            .build();

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

    private HttpResponse doPOST(String url, JsonObject json) throws IOException, URISyntaxException {
        URI uri = buildURI(url);
        HttpPost post = new HttpPost(uri);
        String body = json.toString();
        StringEntity entity = new StringEntity(
                body,
                ContentType.APPLICATION_JSON
        );
        post.setEntity(entity);
        return client.execute(post);
    }

    /* Integration tests */

    @Test
    public void getKanjiWithoutSession() throws Exception {
        int status = doGET("/api/kanji").getStatusLine().getStatusCode();
        Assert.assertEquals(403, status);
    }

    @Test
    public void signUp() throws Exception {
        String expectedResponseBody = factory.createObjectBuilder()
                .add("id", 1)
                .add("name", newUser1.get("username"))
                .build()
                .toString();
        HttpResponse response =  doPOST("/api/signup", newUser1);
        int status = response.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(response.getEntity());
        Assert.assertEquals(201, status);
        Assert.assertEquals(expectedResponseBody, body);
    }

}