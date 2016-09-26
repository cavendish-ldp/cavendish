package cavendish.jetty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.ldp.testsuite.matcher.HttpStatusSuccessMatcher;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Headers;
import com.jayway.restassured.response.Response;

import cavendish.blazegraph.rdf.BufferStatementsHandler;
import cavendish.jetty.matchers.LinkMatcher;

public class VersioningTestCase extends BaseTestCase {
  private static Logger LOG = LoggerFactory.getLogger(VersioningTestCase.class);
  private static final URI HAS_MEMBER = cavendish.ldp.api.Vocabulary.LDP_NS.resolve("#member");

  private String container;
  private String members;

  @Before
  public void createContainers() {
    container = createSubject(getBaseUrl());
    members = createMembersContainer(container);
  }

  @After
  public void resetRest() {
    RestAssured.reset();
  }

  private String createSubject(URL container) {
    String subject = RestAssured
        .given()
            .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
            .header(HttpHeaders.LINK, Link.fromUri(cavendish.ldp.api.Vocabulary.BASIC_CONTAINER).rel(cavendish.ldp.api.Link.REL_TYPE).build().toString())
            .body("<> a <http://example.com/ResourceInteraction> .".getBytes())
        .expect()
            .statusCode(HttpStatusSuccessMatcher.isSuccessful())
            .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
        .post(container)
            .getHeader(HttpHeaders.LOCATION);

    if (subject == null) { // just stop if we can't do member resources
      throw new RuntimeException("no resource created from POST to " + getBaseUrl());
    } else {
      LOG.info("subject basic container: <{}>", subject);
    }
    RestAssured.reset();
    return subject;
  }

  private String createMembersContainer(String container) {
    String rdf = "<> a <http://example.com/ContainerInteraction> ;\n" +
        " <" + cavendish.ldp.api.Vocabulary.MEMBERSHIP_RESOURCE.toString() + "> <" + container + "> ;\n" +
        " <" + cavendish.ldp.api.Vocabulary.HAS_MEMBER_RELATION.toString() + "> <" + HAS_MEMBER.toString() + "> .";
    LOG.debug("inserting member RDF\n{}", rdf);
    String members = RestAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
        .header(HttpHeaders.LINK, Link.fromUri(cavendish.ldp.api.Vocabulary.DIRECT_CONTAINER).rel(cavendish.ldp.api.Link.REL_TYPE).build().toString())
        .body(rdf.getBytes())
        .expect()
        .statusCode(HttpStatusSuccessMatcher.isSuccessful())
        .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
        .post(getBaseUrl())
        .getHeader(HttpHeaders.LOCATION);

    if (members == null) { // just stop if we can't do member resources
      throw new RuntimeException("no resource created from POST to " + getBaseUrl());
    } else {
      LOG.info("direct container for members: <{}>", members);
    }
    RestAssured.reset();
    return members;
  }

  private URI timemapURI(String resourceUri) {
    Headers headers = RestAssured
    .given().expect()
    .statusCode(HttpStatusSuccessMatcher.isSuccessful())
    .head(resourceUri)
    .getHeaders();
    List<String> headerValues = headers.getValues(HttpHeaders.LINK);
    List<Link> linkValues = new ArrayList<Link>(headerValues.size());
    for (String value: headerValues) { linkValues.add(Link.valueOf(value)); }
    URI timemap = null;
    for (Link link: linkValues) {
      if (link.getRel() != null && link.getRel().contains("timemap") ) {
        timemap = link.getUri();
        break;
      }
    }
    RestAssured.reset();
    return timemap;
  }

  private void deleteResource(String resourceUri) {
    RestAssured
    .given().expect()
    .statusCode(HttpStatusSuccessMatcher.isSuccessful())
    .delete(resourceUri);
    RestAssured.reset();
  }

  private String postNoBody(String resourceUri) {
    String result = RestAssured
    .given().expect()
    .statusCode(HttpStatusSuccessMatcher.isSuccessful())
    .post(resourceUri).getHeader(HttpHeaders.LOCATION);
    RestAssured.reset();
    return result;
  }

  private Iterable<Statement> getStatements(String resourceUri) throws OpenRDFException, IOException {
    InputStream is = RestAssured
    .given().expect()
    .statusCode(HttpStatusSuccessMatcher.isSuccessful())
    .get(resourceUri).asInputStream();
    RDFParser parser = new TurtleParser(); // it's the default!
    BufferStatementsHandler buffer = new BufferStatementsHandler();
    parser.setRDFHandler(buffer);
    parser.parse(is, resourceUri);
    return buffer.statements();
  }

  @Test
  public void testTimeMapLink() throws MalformedURLException {
    String subject = createSubject(new URL(this.container));
    URI timemap = timemapURI(subject);

    assertNotNull("No timemap Link header found!", timemap);
    assertTrue(subject + " timemap " + timemap, timemap.getPath().endsWith(URI.create(subject).getPath()));
  }

  @Test
  public void testTimeMapInteractionModels() throws MalformedURLException {
    String subject = createSubject(new URL(this.container));
    URI timemap = timemapURI(subject);
    // it should be type TimeMap and DirectContainer
    Headers headers = RestAssured.given().expect()
    .statusCode(HttpStatusSuccessMatcher.isSuccessful())
    .head(timemap.toURL()).getHeaders();
    List<String> headerValues = headers.getValues(HttpHeaders.LINK);
    List<Link> linkValues = new ArrayList<Link>(headerValues.size());
    for (String value: headerValues) { linkValues.add(Link.valueOf(value)); }
    boolean containerIxn = false;
    boolean timemapIxn = false;
    for (Link link: linkValues) {
      if ("type".equals(link.getRel())) {
        containerIxn |= link.getUri().toString().endsWith("Container");
        timemapIxn |= link.getUri().equals(cavendish.ldp.api.Vocabulary.MEMENTO_TIMEMAP);
      }
    }
    assertTrue("no timemap interaction found for timemap!", timemapIxn);
    assertTrue("no container interaction found for timemap!", containerIxn);
  }

  @Test
  public void testTimeMapSerialization() throws OpenRDFException, IOException {
    String member = createSubject(new URL(this.members));
    // it should create a version resource
    Response mementoResponse = RestAssured
        .given()
            .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
            .header(HttpHeaders.LINK,
                Link.fromUri(cavendish.ldp.api.Vocabulary.BASIC_CONTAINER).rel(cavendish.ldp.api.Link.REL_TYPE).build().toString())
        .expect()
            .statusCode(HttpStatusSuccessMatcher.isSuccessful())
            .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
        .post(timemapURI(this.container));
    String firstMemento = mementoResponse.getHeader(HttpHeaders.LOCATION);
    RestAssured.reset();
    createSubject(new URL(this.members));

    String lastMemento = postNoBody(timemapURI(this.container).toString());

    String body = RestAssured
        .given().header(HttpHeaders.ACCEPT, "application/link-format;q=1.0")
        .expect()
        .header(HttpHeaders.CONTENT_TYPE, "application/link-format")
        .statusCode(HttpStatusSuccessMatcher.isSuccessful()).get(timemapURI(this.container)).asString();
    LOG.info("vv link format body vv\n{}\n^^ link format body ^^", body);
    String [] linkValues = body.split("\\s*,\\s*");
    Link[] links = new Link[linkValues.length];
    for (int i=0; i < linkValues.length; i++) links[i] = Link.valueOf(linkValues[i]);
    boolean firstFound = false;
    boolean lastFound = false;
    boolean originalFound = false;
    boolean selfFound = false;
    for (Link link: links) {
      firstFound |= link.getRel().contains("memento") && link.getRel().contains("first") && link.getUri().toString().equals(firstMemento);
      lastFound |= link.getRel().contains("memento") && link.getRel().contains("last") && link.getUri().toString().equals(lastMemento);
      originalFound |= link.getRel().contains("original") && link.getUri().toString().equals(this.container);
      selfFound |= link.getRel().contains("self") && link.getUri().equals(timemapURI(this.container));
    }
    assertTrue("no link format assertion of original resource", originalFound);
    assertTrue("no link format assertion of context timemap resource", selfFound);
    assertTrue("no link format assertion of first memento resource", firstFound);
    assertTrue("no link format assertion of last memento resource", lastFound);
  }

  @Test
  public void testPostToTimeMap() throws OpenRDFException, IOException {
    String member = createSubject(new URL(this.members));
    // it should create a version resource
    Response mementoResponse = RestAssured
        .given()
            .header(HttpHeaders.CONTENT_TYPE, RDFFormat.TURTLE.getDefaultMIMEType())
            .header(HttpHeaders.LINK, Link.fromUri(cavendish.ldp.api.Vocabulary.BASIC_CONTAINER).rel(cavendish.ldp.api.Link.REL_TYPE).build().toString())
        .expect()
            .statusCode(HttpStatusSuccessMatcher.isSuccessful())
            .header(HttpHeaders.LOCATION, CoreMatchers.notNullValue())
        .post(timemapURI(this.container));
    LinkMatcher typeRel = new LinkMatcher("type", cavendish.ldp.api.Vocabulary.MEMENTO_MEMENTO.toString());
    LinkMatcher mementoRel = new LinkMatcher("memento", this.container);
    mementoResponse.getHeaders().forEach(typeRel);
    mementoResponse.getHeaders().forEach(mementoRel);
    assertTrue("found no memento Link header", mementoRel.isMatched());
    assertTrue("found no type Link header", typeRel.isMatched());
    String memento = mementoResponse.getHeader(HttpHeaders.LOCATION);

    // it should have a member
    ArrayList<Statement> currentStatements = new ArrayList<>();
    for (Statement stmt: getStatements(this.container)) {
      currentStatements.add(stmt);
    }
    // the memento should be identical to the subject graph
    ArrayList<Statement> versionStatements = new ArrayList<>();
    for (Statement stmt: getStatements(memento)) {
      versionStatements.add(stmt);
    }
    assertEquals("memento graph should be the same size", currentStatements.size(), versionStatements.size());
    // if I delete a member, the version should not change
    deleteResource(member);
    versionStatements.clear();
    boolean foundDeleted = false;
    for (Statement stmt: getStatements(memento)) {
      versionStatements.add(stmt);
      if (stmt.getPredicate().stringValue().equals(HAS_MEMBER.toString()) && stmt.getObject().toString().equals(member)) foundDeleted = true;
    }

    assertEquals("memento graph should be the size at time of version", currentStatements.size(), versionStatements.size());
    assertTrue("memento graph should still have membership triple for deleted member " + member, foundDeleted);

    currentStatements.clear();
    for (Statement stmt: getStatements(this.container)) {
      currentStatements.add(stmt);
    }
    assertEquals("original graph should mutate", currentStatements.size() + 1, versionStatements.size());
  }
}
