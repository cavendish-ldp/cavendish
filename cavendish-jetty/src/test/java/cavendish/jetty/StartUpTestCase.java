package cavendish.jetty;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

public class StartUpTestCase extends BaseTestCase {

  @Test
  public void testServerStartup() throws IOException {
    URL url = getBaseUrl();
    URLConnection connection = url.openConnection();
    HttpURLConnection httpConnection = (HttpURLConnection) connection;

    int code = httpConnection.getResponseCode();
    assertEquals(204, code);
  }

}
