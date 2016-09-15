package cavendish.jetty;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.ldp.testsuite.LdpTestSuite;
import org.w3.ldp.testsuite.matcher.HttpStatusSuccessMatcher;
import org.w3.ldp.testsuite.test.LdpTest;

import com.jayway.restassured.RestAssured;

public class LdpTestCase extends BaseTestCase {

  /** @see org.testng.TestNG#HAS_FAILURE */
  private static final int TESTNG_STATUS_HAS_FAILURE = 1;
  /** @see org.testng.TestNG#HAS_SKIPPED */
  private static final int TESTNG_STATUS_HAS_SKIPPED = 2;
  /** @see org.testng.TestNG#HAS_NO_TEST */
  private static final int TESTNG_STATUS_HAS_NO_TEST = 8;

  private static Logger log = LoggerFactory.getLogger(LdpTestCase.class);

  private LdpTestSuite testSuite;

  public String memberResource(String container) {

    String resource = RestAssured
        .given()
            .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
            .header(HttpHeaders.LINK, Link.fromUri(cavendish.ldp.api.Vocabulary.RDF_SOURCE).rel(cavendish.ldp.api.Link.REL_TYPE).build().toString())
            .body("<> a <http://example.com/ResourceInteraction> ; <http://www.w3.org/ns/ldp#MemberSubject> <> .".getBytes())
        .expect()
            .statusCode(HttpStatusSuccessMatcher.isSuccessful())
            .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
        .post(container)
            .getHeader(HttpHeaders.LOCATION);

    if (resource == null) { // just stop if we can't do member resources
      throw new RuntimeException("no resource created from POST to " + getBaseUrl());
    } else {
      log.info("member resource: <{}>", resource);
    }
    RestAssured.reset();
    return resource;
  }

  private String containerAsResource() {
    final String containerAsResource = RestAssured
        .given()
            .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
            .header(HttpHeaders.LINK, Link.fromUri(cavendish.ldp.api.Vocabulary.RDF_SOURCE).rel(cavendish.ldp.api.Link.REL_TYPE).build().toString())
            .body("<> a <http://www.w3.org/ns/ldp#BasicContainer> .".getBytes())
        .expect()
            .statusCode(HttpStatusSuccessMatcher.isSuccessful())
            .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
        .post(getBaseUrl())
            .getHeader(HttpHeaders.LOCATION);

    if (containerAsResource == null) { // just stop if we can't do member resources
      throw new RuntimeException("no resource created from POST to " + getBaseUrl());
    } else {
      log.info("Container as resource: <{}>", containerAsResource);
    }
    RestAssured.reset();
    return containerAsResource;
  }

  private Map<String,String> commonOptions(String containerType, String container) {
    Map<String, String> options = new HashMap<>();
    options.put("server", container);
    options.put("memberResource", memberResource(container));
    options.put("httpLogging", null);
    options.put("skipLogging", null);
    options.put("excludedGroups", LdpTest.MAY);
    // options.put("includedGroups",  LdpTest.MUST + "," + LdpTest.SHOULD)
    String reportPath = new File(new File("target"), containerType).getAbsolutePath();
    options.put("output", reportPath);
    options.put(containerType, "true");
    options.put("cont-res", containerAsResource());
    return options;
  }

  @Test
  public void testBasicContainer() {
    String container = RestAssured
        .given()
            .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
            .header(HttpHeaders.LINK, Link.fromUri(cavendish.ldp.api.Vocabulary.BASIC_CONTAINER).rel(cavendish.ldp.api.Link.REL_TYPE).build().toString())
            .body("<> a <http://example.com/ResourceInteraction> .".getBytes())
        .expect()
            .statusCode(HttpStatusSuccessMatcher.isSuccessful())
            .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
        .post(getBaseUrl())
            .getHeader(HttpHeaders.LOCATION);

    if (container == null) { // just stop if we can't do member resources
      throw new RuntimeException("no resource created from POST to " + getBaseUrl());
    } else {
      log.info("basic container: <{}>", container);
    }
    RestAssured.reset();

    Map<String, String> options = commonOptions("basic", container);
    System.err.println("You can find LDP Test Suite outputs at " + options.get("output"));
    testSuite = new LdpTestSuite(options);
    testSuite.run();
    Assert.assertTrue("ldp-testsuite finished with errors", (testSuite.getStatus() & TESTNG_STATUS_HAS_FAILURE) == 0);
    Assert.assertTrue("ldp-testsuite is empty - no test run", (testSuite.getStatus() & TESTNG_STATUS_HAS_NO_TEST) == 0);
    if ((testSuite.getStatus() & TESTNG_STATUS_HAS_SKIPPED) != 0) {
        log.warn("ldp-testsuite has skipped some tests");
    }
  }

  @Test
  public void testDirectContainer() {
    String rdf = "<> a <http://example.com/ContainerInteraction> ;\n" +
                 " <" + cavendish.ldp.api.Vocabulary.MEMBERSHIP_RESOURCE.toString() + "> <> ;\n" +
                 " <" + cavendish.ldp.api.Vocabulary.HAS_MEMBER_RELATION.toString() + "> <" +
                 cavendish.ldp.api.Vocabulary.LDP.resolve("#member").toString() + "> .";
    System.err.print(rdf);
    String container = RestAssured
        .given()
            .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
            .header(HttpHeaders.LINK, Link.fromUri(cavendish.ldp.api.Vocabulary.DIRECT_CONTAINER).rel(cavendish.ldp.api.Link.REL_TYPE).build().toString())
            .body(rdf.getBytes())
        .expect()
            .statusCode(HttpStatusSuccessMatcher.isSuccessful())
            .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
        .post(getBaseUrl())
            .getHeader(HttpHeaders.LOCATION);

    if (container == null) { // just stop if we can't do member resources
      throw new RuntimeException("no resource created from POST to " + getBaseUrl());
    } else {
      log.info("direct container: <{}>", container);
    }
    RestAssured.reset();

    Map<String, String> options = commonOptions("direct", container);
    System.err.println("You can find LDP Test Suite outputs at " + options.get("output"));
    testSuite = new LdpTestSuite(options);
    testSuite.run();
    Assert.assertTrue("ldp-testsuite finished with errors", (testSuite.getStatus() & TESTNG_STATUS_HAS_FAILURE) == 0);
    Assert.assertTrue("ldp-testsuite is empty - no test run", (testSuite.getStatus() & TESTNG_STATUS_HAS_NO_TEST) == 0);
    if ((testSuite.getStatus() & TESTNG_STATUS_HAS_SKIPPED) != 0) {
        log.warn("ldp-testsuite has skipped some tests");
    }
  }

  @Test
  public void testIndirectContainer() {
    String rdf = "<> a <http://example.com/ContainerInteraction> ;\n" +
                 " <" + cavendish.ldp.api.Vocabulary.MEMBERSHIP_RESOURCE.toString() + "> <> ;\n" +
                 " <" + cavendish.ldp.api.Vocabulary.HAS_MEMBER_RELATION.toString() + "> <" +
                 cavendish.ldp.api.Vocabulary.LDP.resolve("#member").toString() + "> ;\n" +
                 " <" + cavendish.ldp.api.Vocabulary.INSERTED_CONTENT_RELATION.toString() + "> <" +
                 cavendish.ldp.api.Vocabulary.LDP.resolve("#MemberSubject").toString() + "> .";
    System.err.print(rdf);
    String container = RestAssured
        .given()
            .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
            .header(HttpHeaders.LINK, Link.fromUri(cavendish.ldp.api.Vocabulary.INDIRECT_CONTAINER).rel(cavendish.ldp.api.Link.REL_TYPE).build().toString())
            .body(rdf.getBytes())
        .expect()
            .statusCode(HttpStatusSuccessMatcher.isSuccessful())
            .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
        .post(getBaseUrl())
            .getHeader(HttpHeaders.LOCATION);

    if (container == null) { // just stop if we can't do member resources
      throw new RuntimeException("no resource created from POST to " + getBaseUrl());
    } else {
      log.info("indirect container: <{}>", container);
    }
    RestAssured.reset();

    Map<String, String> options = commonOptions("indirect", container);
    System.err.println("You can find LDP Test Suite outputs at " + options.get("output"));
    testSuite = new LdpTestSuite(options);
    testSuite.run();
    Assert.assertTrue("ldp-testsuite finished with errors", (testSuite.getStatus() & TESTNG_STATUS_HAS_FAILURE) == 0);
    Assert.assertTrue("ldp-testsuite is empty - no test run", (testSuite.getStatus() & TESTNG_STATUS_HAS_NO_TEST) == 0);
    if ((testSuite.getStatus() & TESTNG_STATUS_HAS_SKIPPED) != 0) {
        log.warn("ldp-testsuite has skipped some tests");
    }
  }
}
