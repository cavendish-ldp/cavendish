package cavendish.jetty;

import static org.junit.Assert.fail;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.ldp.testsuite.matcher.HttpStatusSuccessMatcher;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Header;

import cavendish.jetty.headers.HeaderValue;
import cavendish.ldp.api.LdpHeaders;

public class NonRDFTestCase extends BaseTestCase {
  private static Logger LOG = LoggerFactory.getLogger(NonRDFTestCase.class);

  private String containerUrl = null;

  @Before
  public void createTestContainer() {
    containerUrl = RestAssured
        .given()
            .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
            .header(HttpHeaders.LINK, Link.fromUri(cavendish.ldp.api.Vocabulary.CONTAINER).rel(cavendish.ldp.api.Link.REL_TYPE).build().toString())
            .body("<> a <http://example.com/ResourceInteraction> .".getBytes())
        .expect()
            .statusCode(HttpStatusSuccessMatcher.isSuccessful())
            .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
        .post(getBaseUrl())
            .getHeader(HttpHeaders.LOCATION);

    if (containerUrl == null) { // just stop if we can't do member resources
      throw new RuntimeException("no resource created from POST to " + getBaseUrl());
    } else {
      LOG.info("testing nonRdfSource to container: <{}>", containerUrl);
    }
    RestAssured.reset();
  }

  @Test
  public void testAcceptPost() {
    List<Header> acceptPost = RestAssured
        .expect()
            .statusCode(HttpStatusSuccessMatcher.isSuccessful())
        .head(containerUrl)
            .getHeaders().getList(LdpHeaders.ACCEPT_POST);

    if (acceptPost == null || acceptPost.isEmpty()) { // just stop if we can't do member resources
      throw new RuntimeException("no Accept-Post header from HEAD to " + containerUrl);
    }
    RestAssured.reset();
    for (Header header: acceptPost) {
      HeaderValue value = HeaderValue.parse(header.getValue());
      if (value.value().equals("message/external-body")) return;
    }
    fail("Expected at least one Accept-Post values for message/external-body");
  }
}
