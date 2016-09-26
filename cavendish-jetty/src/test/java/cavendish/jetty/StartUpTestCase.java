package cavendish.jetty;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.w3.ldp.testsuite.matcher.HttpStatusSuccessMatcher;

import com.jayway.restassured.RestAssured;

public class StartUpTestCase extends BaseTestCase {

  @Test
  public void testServerStartup() throws IOException {
    assertTrue(this.app.getServer().isStarted());
    URL url = getBaseUrl();
    RestAssured.expect().statusCode(HttpStatusSuccessMatcher.isSuccessful()).get(url);
  }

}
