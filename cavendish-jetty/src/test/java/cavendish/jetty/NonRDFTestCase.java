package cavendish.jetty;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import com.jayway.restassured.response.Response;

import cavendish.ldp.impl.HeaderValue;
import cavendish.ldp.api.LdpHeaders;
import cavendish.ldp.api.Vocabulary;

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
      HeaderValue[] values = HeaderValue.parseValues(header.getValue());
      for (HeaderValue value: values) {
        LOG.info(value.toString());
        if (value.value().equals("message/external-body")) return;
      }
    }
    fail("Expected at least one Accept-Post values for message/external-body");
  }
  @Test
  public void testCreateNonRdfSource() throws IOException {
    String testTextContent = "test text content";
    File tempFile = File.createTempFile("testCreateNonRdfSource", "txt");
    try(FileWriter writer = new FileWriter(tempFile)) {
      writer.write(testTextContent);
    }
    Response response = RestAssured
        .with().header(HttpHeaders.LINK, Vocabulary.NON_RDF_SOURCE.toString() + ";rel=type")
        .with().header(HttpHeaders.CONTENT_TYPE, "text/plain")
        .with().header(HttpHeaders.CONTENT_TYPE, "message/external-body;access-type=URL;URL=" + tempFile.toURI())
        .expect()
        .statusCode(HttpStatusSuccessMatcher.isSuccessful())
        .post(containerUrl);
    String createdResource = response.getHeader(HttpHeaders.LOCATION);
    RestAssured.reset();
    RestAssured.expect()
        .statusCode(HttpStatusSuccessMatcher.isSuccessful())
        .header(HttpHeaders.LINK, Link.fromUri(cavendish.ldp.api.Vocabulary.NON_RDF_SOURCE).rel(cavendish.ldp.api.Link.REL_TYPE).build().toString())
        .header(HttpHeaders.LOCATION, tempFile.toURI().toString())
        .header(HttpHeaders.CONTENT_LENGTH, Integer.toString(testTextContent.getBytes().length))
        .get(createdResource);
  }
}
