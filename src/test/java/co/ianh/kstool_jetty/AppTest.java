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
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import javax.json.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by henderson_i on 4/12/16.
 */
@RunWith(Enclosed.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppTest {

    // abstract inner class ignored by Enclosed runner:
    // http://stackoverflow.com/questions/26775984/junit-enclosed-runner-and-shared-setup
    abstract public static class SharedSetup {

        static JsonBuilderFactory factory = Json.createBuilderFactory(null);
        static JsonObject newUser1 = factory.createObjectBuilder()
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
        static CloseableHttpClient client;
        static Server app;
        String HOSTNAME = "localhost";
        int PORT = 8000; // TODO: tightly coupled; app always runs on 8000

        // Start up server
        @Before
        public void setUp() throws Exception {
            app = App.makeServer();
            client = HttpClients.createDefault();
        }

        // Shut down server
        @After
        public void tearDown() throws Exception {
            System.out.println("All tore down, brah.");
            app.stop();
        }

        /* Helper methods */

        URI buildURI(String url) throws URISyntaxException {
            return new URIBuilder()
                    .setScheme("http")
                    .setHost(HOSTNAME)
                    .setPort(PORT)
                    .setPath(url)
                    .build();
        }

        HttpResponse doGET(String url) throws IOException, URISyntaxException {
            URI uri = buildURI(url);
            HttpGet get = new HttpGet(uri);
            return client.execute(get);
        }

        HttpResponse doPOST(String url) throws IOException, URISyntaxException {
            URI uri = buildURI(url);
            HttpPost post = new HttpPost(uri);
            return client.execute(post);
        }

        HttpResponse doPOST(String url, JsonObject json) throws IOException, URISyntaxException {
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


    }

    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class NonParameterizedTests extends SharedSetup{

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

        @Test
        public void signInWithWrongInfo() throws Exception {
            JsonObject wrongInfo = factory.createObjectBuilder()
                    .add("username", newUser1.get("username"))
                    .add("password", "abcdefg")
                    .build();

            HttpResponse response = doPOST("/api/login", wrongInfo);
            int status = response.getStatusLine().getStatusCode();

            Assert.assertEquals(403, status);
        }

        @Test
        public void signInWithCorrectInfo() throws Exception {
            JsonObject correctInfo = factory.createObjectBuilder()
                    .add("username", newUser1.get("username"))
                    .add("password", newUser1.get("password"))
                    .build();

            String expectedResponseBody = factory.createObjectBuilder()
                    .add("id", 1)
                    .add("name", newUser1.get("username"))
                    .build()
                    .toString();

            HttpResponse response = doPOST("/api/login", correctInfo);
            int status = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity());

            Assert.assertEquals(200, status);
            Assert.assertEquals(expectedResponseBody, body);
        }

        @Test
        public void getKanjiWhenListIsEmpty() throws Exception {
            HttpResponse response = doGET("/api/kanji");
            int status = response.getStatusLine().getStatusCode();

            Assert.assertEquals(404, status);
        }

        @Test
        public void addWords() throws Exception {
            JsonObject fact = factory.createObjectBuilder()
                    .add("fact", newUser1.get("fact"))
                    .build();

            HttpResponse response = doPOST("api/facts", fact);
            int status = response.getStatusLine().getStatusCode();

            Assert.assertEquals(201, status);
        }

    }

    @RunWith(Parameterized.class)
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class ParameterizedTests extends SharedSetup{

        private String parameterizedKanji;

        @Parameterized.Parameters(name = "getNextKanji: {0}")
        public static List<String> data() {
            String kanji = newUser1.get("fact_stripped").toString();
            return Arrays.asList(kanji.split("")); // 日本語盛上
        }

        public ParameterizedTests(String parameterizedKanji) {
            this.parameterizedKanji = parameterizedKanji;
        }


        @Test
        public void getNextKanji() throws Exception {
            String expectedBody = factory.createObjectBuilder()
                    .add("kanji", parameterizedKanji)
                    .add("words", newUser1.get("fact"))
                    .build()
                    .toString();

            HttpResponse response = doGET("api/kanji");
            int status = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity());

            Assert.assertEquals(200, status);
            Assert.assertEquals(expectedBody, body);
        }

    }


}