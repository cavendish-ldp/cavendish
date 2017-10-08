package cavendish.jetty.handler;

import static cavendish.ldp.api.Vocabulary.InteractionType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.jboss.resteasy.util.AcceptParser;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.turtle.TurtleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cavendish.blazegraph.ldp.Vocabulary;
import cavendish.blazegraph.rdf.BufferStatementsHandler;
import cavendish.blazegraph.rdf.ConstraintViolationException;
import cavendish.blazegraph.task.DeleteRdfSourceTask;
import cavendish.blazegraph.task.InsertMementoTask;
import cavendish.blazegraph.task.InsertRdfSourceTask;
import cavendish.blazegraph.task.InteractionModelsQueryTask;
import cavendish.blazegraph.task.LdpResourceExistsTask;
import cavendish.blazegraph.task.RDFFormattable;
import cavendish.blazegraph.task.ReplaceRdfSourceTask;
import cavendish.blazegraph.task.SubjectStatementsQueryTask;
import cavendish.blazegraph.task.UpdateRdfSourceTask;
import cavendish.jetty.ContentTypes;
import cavendish.ldp.api.LdpHeaders;
import cavendish.ldp.api.SerializationPreference;
import cavendish.ldp.impl.Prefer;

import com.bigdata.journal.IBufferStrategy;
import com.bigdata.journal.IIndexManager;
import com.bigdata.rdf.axioms.NoAxioms;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.CreateKBTask;
import com.bigdata.rdf.sail.sparql.Bigdata2ASTSPARQLParser;
import com.bigdata.rdf.sail.webapp.NanoSparqlServer;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.rdf.task.AbstractApiTask;
import com.github.jsonldjava.sesame.SesameJSONLDParser;

import org.openrdf.model.vocabulary.RDF;

public class LdpHandler extends AbstractHandler {

  private final static Logger LOG = LoggerFactory.getLogger(LdpHandler.class);

  private final static String CONSTRAINTS_PATH = "/constraints";

  private final static String TIMEMAPS_PATH = "/timemaps";

  private final static String DEFAULT_NS = "kb";

  private final static String ETAG = "ETAG";

  public final static String LDP_PATH = "/ldp";

  private IIndexManager indexManager;

  private final String[] rootPaths;
  public LdpHandler() {
    this(new String[]{LDP_PATH});
  }
  public LdpHandler(String[] rootPaths) {
    this.rootPaths = rootPaths;
    RDFFormattable.registerTimeMapFormat();
    RDFFormattable.registerApplicationNTriplesFormat();
  }

  public void setIndexManager(IIndexManager indexManager) {
    this.indexManager = indexManager;  
  }

  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String root = rootNode(request);
    if (root != null) {
      LOG.info("{} target -> {} {}", new String[]{request.getMethod(), target, request.getRequestURI()});
      try{
        ensureRoot(request, root);
      } catch (Exception e) {
        handleException(e, baseRequest, request, response);
        LOG.error(e.getMessage(),e);
        e.printStackTrace();
        return;
      }
      if (request.getMethod().equals("DELETE")) {
        try {
          doDELETE(target, baseRequest, request, response);
        } catch (Exception e) {
          handleException(e, baseRequest, request, response);
        }
      }
      else if (request.getMethod().equals("GET")) {
        try {
          doGET(target, baseRequest, request, response);
        } catch (Exception e) {
          handleException(e, baseRequest, request, response);
        }
      }
      else if (request.getMethod().equals("HEAD")) {
        try {
          doHEAD(target, baseRequest, request, response);
        } catch (Exception e) {
          handleException(e, baseRequest, request, response);
        }
      }
      else if (request.getMethod().equals("OPTIONS")) {
        try {
          doOPTIONS(target, baseRequest, request, response);
        } catch (Exception e) {
          handleException(e, baseRequest, request, response);
        }
      }
      else if (request.getMethod().equals("PATCH")) {
        try {
          doPATCH(target, baseRequest, request, response);
        } catch (Exception e) {
          handleException(e, baseRequest, request, response);
        }
      }
      else if (request.getMethod().equals("POST")) {
        try {
          doPOST(target, baseRequest, request, response);
        } catch (Exception e) {
          handleException(e, baseRequest, request, response);
        }
      }
      else if (request.getMethod().equals("PUT")) {
        try {
          doPUT(target, baseRequest, request, response);
        } catch (Exception e) {
          handleException(e, baseRequest, request, response);
        }
      }
      baseRequest.setHandled(true);
    } else if (request.getPathInfo().equals(CONSTRAINTS_PATH)) {
      try {
        doConstraints(target, baseRequest, request, response);
      } catch (Exception e) {
        handleException(e, baseRequest, request, response);
      }
      baseRequest.setHandled(true);
    } else {
      LOG.info("unexpected request.getPathInfo() -> {}", request.getPathInfo());
    }
  }

  protected String rootNode(HttpServletRequest request) {
    for (String root: this.rootPaths) {
      if (request.getRequestURI().equals(root)) return root;
      if (request.getRequestURI().startsWith(root + "/")) return root;
    }
    return null;
  }

  protected void ensureRoot(HttpServletRequest request, String root) throws Exception {
    StringBuilder b = createContext(request);
    if (root != null) b.append(root);
    String rootResource = b.toString();
    int status = resourceStatus(rootResource, 0L);

    if (HttpServletResponse.SC_NOT_FOUND == status) {
      URIImpl subject = new URIImpl(rootResource);
      BufferStatementsHandler buffer = new BufferStatementsHandler();
      buffer.handleStatement(new ContextStatementImpl(subject, RDF.TYPE, Vocabulary.RDF_SOURCE, null));
      buffer.handleStatement(new ContextStatementImpl(subject, cavendish.blazegraph.ldp.Vocabulary.IANA_TYPE, Vocabulary.BASIC_CONTAINER, Vocabulary.INTERNAL_CONTEXT));
      buffer.handleStatement(new ContextStatementImpl(subject, cavendish.blazegraph.ldp.Vocabulary.IANA_TYPE, Vocabulary.RDF_SOURCE, Vocabulary.INTERNAL_CONTEXT));
      buffer.handleStatement(new ContextStatementImpl(subject, cavendish.blazegraph.ldp.Vocabulary.IANA_TYPE, Vocabulary.RESOURCE, Vocabulary.INTERNAL_CONTEXT));
      InsertRdfSourceTask task = new InsertRdfSourceTask(DEFAULT_NS, true, subject, buffer.iterate());
      AbstractApiTask.submitApiTask(indexManager, task).get();
      LOG.info("ensured existence of root node {}", rootResource);
    }

  }

  protected void doDELETE(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    String rootUri = request.getRequestURI();
    rootUri = rootUri.substring(0,rootUri.indexOf(LDP_PATH) + 4);
    if (rootUri.isEmpty()) {
      throw new NotAllowedException("DELETE Unimplemented for root node");
    }
    URI subject = new URIImpl(this.requestResource(request));
    DeleteRdfSourceTask task = new DeleteRdfSourceTask(DEFAULT_NS, true, subject);
    AbstractApiTask.submitApiTask(indexManager, task).get();
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  protected void doGET(String target, Request baseRequest, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    ContentType ct = responseType(request);
    String path = request.getPathInfo();
    if (CONSTRAINTS_PATH.equals(path)) {
      doConstraints(target, baseRequest, request, response);
      return;
    }
    Iterator<Statement> stmts = null;
    long timestamp = indexManager.getLastCommitTime();

    URI subject = new URIImpl(this.requestResource(request));
    LOG.debug("request subject >>{}<<", subject);
    Stream<Statement> ixnModels = AbstractApiTask.submitApiTask(indexManager, new InteractionModelsQueryTask(DEFAULT_NS, timestamp, false, subject)).get();
    InteractionType containerType = reduceModels(ixnModels);
    SerializationPreference prefs = (request.getHeader(LdpHeaders.PREFER) != null) ?
        Prefer.parse(request.getHeader(LdpHeaders.PREFER)) : (containerType == InteractionType.TIMEMAP) ?
            SubjectStatementsQueryTask.DEFAULT_TIMEMAP_PREFS : SubjectStatementsQueryTask.DEFAULT_PREFS;
    LOG.debug("Prefer: >>{}<<", prefs);

    if (!path.equals("")) {
      int sc = resourceStatus(subject,timestamp);
      switch(sc) {
      case HttpServletResponse.SC_GONE:
        response.setContentType(ct.getMimeType());
        response.setStatus(HttpServletResponse.SC_GONE);
        response.setContentLength(0);
        return;
      case HttpServletResponse.SC_NOT_FOUND:
        throw new NotFoundException(this.requestResource(request));
      }
    }

    SubjectStatementsQueryTask task = new SubjectStatementsQueryTask(DEFAULT_NS, timestamp, false, subject, prefs);
    stmts = AbstractApiTask.submitApiTask(indexManager, task).get();
    responseLinkHeader(subject, request, response, timestamp);

    if (prefs.wasAcknowledged()) response.addHeader(LdpHeaders.PREFERENCE_APPLIED, "return=representation");
    response.addHeader(ETAG, weakETag(subject.toString()));

    optionsHeaders(response, containerType);
    response.setContentType(ct.getMimeType());

    RDFWriter writer = RDFFormattable.getResponseWriter(ct.getMimeType(), response.getOutputStream());
    writer.startRDF();
    while (stmts.hasNext()) {
      Statement stmt = stmts.next();
      writer.handleStatement(stmt);
    }
    writer.endRDF();
    response.getOutputStream().flush();
  }

  protected void doHEAD(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    ContentType ct = responseType(request);
    String path = request.getPathInfo();
    URI subject = new URIImpl(this.requestResource(request));
    if (path.equals(LDP_PATH)) {
      response.setContentType(ct.getMimeType());
      responseLinkHeader(subject, request, response, indexManager.getLastCommitTime());
      optionsHeaders(response, InteractionType.CONTAINER);
      response.addHeader(ETAG, weakETag(request.getRequestURL().toString()));
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentLength(0);
    } else {
      long timestamp = indexManager.getLastCommitTime();

      int sc = AbstractApiTask.submitApiTask(indexManager, new LdpResourceExistsTask(DEFAULT_NS, timestamp, false, subject)).get();
      switch(sc) {
      case HttpServletResponse.SC_GONE:
        response.setContentType(ct.getMimeType());
        response.setStatus(HttpServletResponse.SC_GONE);
        response.setContentLength(0);
        return;
      case HttpServletResponse.SC_NOT_FOUND:
        throw new NotFoundException(this.requestResource(request));
      default:
        response.setContentType(ct.getMimeType());
        responseLinkHeader(subject, request, response, timestamp);
        response.addHeader(ETAG, weakETag(subject.toString()));
        Stream<Statement> ixnModels = AbstractApiTask.submitApiTask(indexManager, new InteractionModelsQueryTask(DEFAULT_NS, timestamp, false, subject)).get();
        optionsHeaders(response, reduceModels(ixnModels));
        response.setContentLength(0);
      }
    }
  }

  private InteractionType reduceModels(Stream<Statement> ixnModels) {
    return ixnModels.map(new Function<Statement, InteractionType>() {

      @Override
      public InteractionType apply(Statement stmt) {
        String ixnModel = stmt.getObject().stringValue();
        if (ixnModel.endsWith("TimeMap")) return InteractionType.TIMEMAP;
        if (ixnModel.endsWith("Container")) return InteractionType.CONTAINER;
        if (ixnModel.endsWith("Memento")) return InteractionType.MEMENTO;
        if (ixnModel.endsWith("RDFSource")) return InteractionType.RDFSOURCE;
        return InteractionType.RESOURCE;
      }

    }).max(new Comparator<InteractionType>(){
      @Override
      public int compare(InteractionType o1, InteractionType o2) {
        if (o1 == o2) return 0;
        if (o1 == InteractionType.TIMEMAP) return 1;
        if (o2 == InteractionType.TIMEMAP) return -1;
        if (o1 == InteractionType.CONTAINER) return 1;
        if (o2 == InteractionType.CONTAINER) return -1;
        if (o1 == InteractionType.MEMENTO) return 1;
        if (o2 == InteractionType.MEMENTO) return -1;
        if (o1 == InteractionType.RDFSOURCE) return 1;
        if (o2 == InteractionType.RDFSOURCE) return -1;
        return -1;
      }
    }
        ).orElse(InteractionType.RESOURCE);
  }

  protected void doOPTIONS(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws Exception {
    URI subject = new URIImpl(requestResource(request));
    Stream<Statement> ixnModels = AbstractApiTask.submitApiTask(indexManager, new InteractionModelsQueryTask(DEFAULT_NS, indexManager.getLastCommitTime(), false, subject)).get();
    optionsHeaders(response, reduceModels(ixnModels));
    response.setStatus(HttpServletResponse.SC_OK);
  }

  protected void doPATCH(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws Exception {
    String resource = requestResource(request);
    int status = resourceStatus(resource, 0L);
    if (HttpServletResponse.SC_OK != status) {
      response.setStatus(status);
      return;
    }

    ContentType ct = requestContentType(request);
    if (!ct.getMimeType().equals("application/sparql-update")) {
      throw new BadRequestException("Unsupported PATCH entity type: " + ct.getMimeType());
    }
    URIImpl subject = new URIImpl(resource);
    if (subject.stringValue().equals(resource)) {
      throw new UnsupportedOperationException("PATCH not yet implemented");
    }
    String updateSrc = IOUtils.toString(request.getInputStream());
    Bigdata2ASTSPARQLParser parser = new Bigdata2ASTSPARQLParser();
    ParsedUpdate operation = parser.parseUpdate(updateSrc, resource);
    //TODO some filtering
    List<UpdateExpr> updates = operation.getUpdateExprs();
    AbstractApiTask.submitApiTask(indexManager, new UpdateRdfSourceTask(DEFAULT_NS, true, subject, updates));
  }

  protected void doPOST(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws Exception {
    URIImpl subject = new URIImpl(this.requestResource(request));
    long timestamp = indexManager.getLastCommitTime();
    int sc = AbstractApiTask.submitApiTask(indexManager, new LdpResourceExistsTask(DEFAULT_NS, timestamp, false, subject)).get();
    switch(sc) {
    case HttpServletResponse.SC_GONE:
      response.setStatus(HttpServletResponse.SC_GONE);
      response.setContentLength(0);
      return;
    case HttpServletResponse.SC_NOT_FOUND:
      throw new NotFoundException(this.requestResource(request));
    }

    Stream<Statement> ixnModels = AbstractApiTask.submitApiTask(indexManager, new InteractionModelsQueryTask(DEFAULT_NS, timestamp, false, subject)).get();
    InteractionType containerType = reduceModels(ixnModels);
    if (containerType != InteractionType.CONTAINER && containerType != InteractionType.TIMEMAP) {
      throw new NotAllowedException("Only Container resources support POST");
    }

    // insert the new context
    switch(containerType) {
    case TIMEMAP:
      createMemento(subject, request, response);
      break;
    case CONTAINER:
      String resource = resourceToCreate(request, containerType);

      int status = resourceStatus(resource, timestamp);
      // do not allow reuse of a URI
      if (status == HttpServletResponse.SC_GONE) {
        resource = resource.substring(0,resource.lastIndexOf('/') + 1);
        resource = resource + slug(null, containerType);
      }
      createResource(new URIImpl(resource), request, response);
      break;
    default:
      throw new BadRequestException("Unsupported interaction type on POST: " + containerType.name());
    }
  }

  private void createResource(URIImpl resourceToCreate, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    BufferStatementsHandler buffer = parseRDFBody(request, resourceToCreate.stringValue());
    ianaTypeStatements(request, resourceToCreate, buffer);

    InsertRdfSourceTask task = new InsertRdfSourceTask(DEFAULT_NS, true, resourceToCreate, buffer.iterate());
    AbstractApiTask.submitApiTask(indexManager, task).get();
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setHeader(HttpHeaders.LOCATION, resourceToCreate.stringValue());
    responseLinkHeader(resourceToCreate, request, response, indexManager.getLastCommitTime());
    response.setContentType("text/turtle");
  }

  private void createMemento(URIImpl timemap, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    InsertMementoTask task = new InsertMementoTask(DEFAULT_NS, true, timemap);
    Statement mementoFor = AbstractApiTask.submitApiTask(indexManager, task).get();
    URI memento = (URI) mementoFor.getSubject();
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setHeader(HttpHeaders.LOCATION, memento.stringValue());
    addLinkHeaders(request, response, "memento", (URI) mementoFor.getObject());
    responseLinkHeader(memento, request, response, indexManager.getLastCommitTime());
    response.setContentType("text/turtle");
  }

  protected void doPUT(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    response.setContentType("text/turtle");
    String resource = requestResource(request);
    if (request.getHeader(LdpHeaders.SLUG) != null) {
      throw new BadRequestException("PUT cannot create new resources with Slug");
    }
    int status = resourceStatus(resource, 0L);
    if (HttpServletResponse.SC_GONE == status) {
      response.setStatus(HttpServletResponse.SC_GONE);
      return;
    }
    if (HttpServletResponse.SC_NOT_FOUND == status) {
      // reluctantly handle PUT to create
      // insert the new context
      createResource(new URIImpl(resource), request, response);
      return;
    }
    if (HttpServletResponse.SC_OK == status) {
      if (request.getHeader(HttpHeaders.IF_MATCH) == null) {
        throw new ClientErrorException("PUT requires If-Match header", Vocabulary.SC_PRECONDITION_REQUIRED);
      }
    }
    if (resourceChangedSince(resource, request)) {
      throw new ClientErrorException(HttpServletResponse.SC_PRECONDITION_FAILED);
    }
    if (!request.getRequestURI().equals(rootNode(request))) {
      String container = resource.substring(0,resource.lastIndexOf('/'));
      int cStatus = 200;
      if (HttpServletResponse.SC_OK != (cStatus = resourceStatus(container, 0L))) {
        throw new BadRequestException("Container status was no good: " + container + " " + cStatus);
      }
    }

    BufferStatementsHandler buffer = parseRDFBody(request, resource);
    URIImpl subject = new URIImpl(resource);
    ianaTypeStatements(request, subject, buffer);
    for (Statement s: buffer.statements()) {
      if (Vocabulary.MEMENTO_MEMENTO.equals(s.getObject())) throw new BadRequestException("Mementos are not modifiable");
    }

    ReplaceRdfSourceTask task = new ReplaceRdfSourceTask(DEFAULT_NS, true, subject, buffer.iterate());
    AbstractApiTask.submitApiTask(indexManager, task).get();
    if (status == 200) {
      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      response.setStatus(HttpServletResponse.SC_CREATED);
    }
    response.setHeader(HttpHeaders.LOCATION, resource);
    responseLinkHeader(subject, request, response, System.currentTimeMillis());
    response.setContentType("text/turtle");
    LOG.info(" response Location -> {}", resource);
  }

  protected void doConstraints(String target, Request baseRequest, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    ServletOutputStream out = response.getOutputStream();
    out.print("don't send me ldp:contains triples or iana:type triples either");
    out.flush();
  }

  protected BufferStatementsHandler parseRDFBody(HttpServletRequest request, String resource)
      throws Exception {
    ContentType ct = requestContentType(request);
    // insert the new context
    RDFParser parser;
    if (RDFFormat.TURTLE.getMIMETypes().contains(ct.getMimeType())) {
      parser = new TurtleParser();
    } else if (RDFFormat.JSONLD.getMIMETypes().contains(ct.getMimeType())) {
      parser = new SesameJSONLDParser();
    } else {
      LOG.info("{} not supported", ct.getMimeType());
      throw new NotSupportedException("Unsupported Content Type " + ct.getMimeType());
    }

    ServletInputStream is = request.getInputStream();
    BufferStatementsHandler buffer = new BufferStatementsHandler();
    parser.setRDFHandler(buffer);
    parser.parse(is, resource);
    return buffer;
  }

  private static boolean wrapperException(Throwable t) {
    if (t.getCause() == null) return false;
    if (t instanceof java.util.concurrent.ExecutionException) return true;
    if (t instanceof java.util.concurrent.CompletionException) return true;
    if (t instanceof RuntimeException) return true;
    return false;
  }
  protected void handleException(Exception e, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
    Throwable t = e;
    while(wrapperException(t)) t = t.getCause();
    t.printStackTrace();

    try {
      if (t instanceof NotAllowedException) {
        LOG.info(t.getMessage(),t);
        response.getOutputStream().print(t.toString());
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      }
      else if (t instanceof NotSupportedException) {
        LOG.info(t.getMessage(),t);
        response.getOutputStream().print(t.toString());
        response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
      }
      else if (t instanceof NotFoundException) {
        LOG.warn(t.getMessage(),t);
        response.getOutputStream().print(t.toString());
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
      else if (t instanceof BadRequestException) {
        LOG.info(t.getMessage(),t);
        response.getOutputStream().print(t.toString());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
      else if (t instanceof ClientErrorException) {
        LOG.info(t.getMessage(),t);
        response.getOutputStream().print(t.toString());
        response.setStatus(((ClientErrorException)t).getResponse().getStatus());
      }
      else if (t instanceof ConstraintViolationException) {
        LOG.info(t.getMessage(),t);
        response.getOutputStream().print(t.toString());
        response.setStatus(HttpServletResponse.SC_CONFLICT);
      }
      else {
        LOG.error(t.getMessage(),t);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      addLinkHeaders(baseRequest, response, Vocabulary.CONSTRAINED_BY, new URIImpl(constraintsUri(baseRequest)));
    } catch (IOException ioe) {
      LOG.error(ioe.getMessage(), ioe);
    }
  }

  /* ------------------------------------------------------------ */
  /* 
   * @see org.eclipse.thread.LifeCycle#start()
   */
  @Override
  protected void doStart() throws Exception
  {
    super.doStart();
    LOG.debug("starting {}", this);

    if (indexManager == null) {
      indexManager = (IIndexManager) NanoSparqlServer.getWebApp(getServer()).getAttribute(IIndexManager.class.getName());
    }

    if (indexManager == null) {
      LOG.warn("No IndexManager available for {}", this);
      throw new Exception("needs indexManager to run LdpHandler");
    }
    com.bigdata.journal.Journal j = (com.bigdata.journal.Journal)indexManager;
    LOG.info("indexManager readOnly {}", Boolean.toString(j.isReadOnly()));
    IBufferStrategy strat = j.getBufferStrategy();
    LOG.info("buffer strategy {} readOnly {}", strat.getClass().getName(), Boolean.toString(strat.isReadOnly()));
    Properties props = new Properties();
    props.setProperty(AbstractTripleStore.Options.QUADS, Boolean.toString(true));
    props.setProperty(AbstractTripleStore.Options.AXIOMS_CLASS, NoAxioms.class.getName());
    //props.setProperty(AbstractTripleStore.Options.STATEMENT_IDENTIFIERS, Boolean.toString(false));
    //props.setProperty(AbstractTripleStore.Options.TRIPLES_MODE_WITH_PROVENANCE, Boolean.toString(true));
    props.setProperty(BigdataSail.Options.TRUTH_MAINTENANCE,Boolean.toString(false));
    CreateKBTask task = new CreateKBTask(DEFAULT_NS, props);
    try {
      AbstractApiTask.submitApiTask(indexManager, task).get();
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
    }
  }
  protected int resourceStatus(String resource, long timestamp) throws InterruptedException, ExecutionException {
    return resourceStatus(new URIImpl(resource), timestamp);
  }
  protected int resourceStatus(URI resource, long timestamp) throws InterruptedException, ExecutionException {
    return AbstractApiTask.submitApiTask(indexManager, new LdpResourceExistsTask(DEFAULT_NS, timestamp, false, resource)).get();
  }

  protected String requestResource(HttpServletRequest request) {
    StringBuilder b = new StringBuilder();
    b.append(request.getScheme()).append("://").append(request.getServerName());
    if ("http".equals(request.getScheme()) && 80 != request.getServerPort()) {
      b.append(':').append(Integer.toString(request.getServerPort()));
    }
    if ("https".equals(request.getScheme()) && 443 != request.getServerPort()) {
      b.append(':').append(Integer.toString(request.getServerPort()));
    }
    try {
      // the URL encoding will not match the inserted URI unless the path is decoded
      b.append(java.net.URLDecoder.decode(request.getRequestURI(),"UTF8"));
    } catch (UnsupportedEncodingException e) {} // come on

    if (request.getRequestURI().endsWith("/")) b.deleteCharAt(b.length() - 1);
    return b.toString();
  }

  protected ContentType requestContentType(HttpServletRequest request) {
    String contentType = request.getContentType();
    ContentType ct = (contentType == null || contentType.isEmpty()) ?
        ContentType.create(RDFFormat.TURTLE.getDefaultMIMEType(), Charset.forName("UTF8")) :
          ContentType.parse(contentType);
        return ct;
  }

  protected String weakETag(String subject) {
    String hash = DigestUtils.md5Hex(Long.toString(indexManager.getLastCommitTime()) + ';' + subject);
    return "W/\"" + hash + '"';
  }

  protected boolean resourceChangedSince(String subject, HttpServletRequest request) {
    String condition = request.getHeader(HttpHeaders.IF_MATCH);
    if (condition != null) {
      String etag = weakETag(subject);
      if (!etag.equals(condition)) {
        LOG.info("{} changed since condition {}", subject, condition);
        return true;
      }
    }
    return false;
  }

  protected Stream<Statement> ianaTypeStatements(URI subject, long timestamp) throws Exception {
    InteractionModelsQueryTask task = new InteractionModelsQueryTask(DEFAULT_NS, timestamp, false, subject);
    return AbstractApiTask.submitApiTask(indexManager, task).get();
  }

  static void optionsHeaders(HttpServletResponse response, InteractionType ixnType) {
    switch(ixnType) {
    case CONTAINER:
      response.setHeader(HttpHeaders.ALLOW, "GET,HEAD,OPTIONS,PATCH,POST,PUT");
      response.setHeader(LdpHeaders.ACCEPT_POST, "text/turtle,application/ld+json");
      response.setHeader(LdpHeaders.ACCEPT_PATCH, "application/sparql-update");
      break;
    case TIMEMAP:
      response.setHeader(HttpHeaders.ALLOW, "GET,HEAD,OPTIONS,POST");
      response.setHeader(LdpHeaders.ACCEPT_POST, "*/*; p=0.0");
      break;
    case RDFSOURCE:
      response.setHeader(HttpHeaders.ALLOW, "GET,HEAD,OPTIONS,PATCH,PUT");
      response.setHeader(LdpHeaders.ACCEPT_PATCH, "application/sparql-update");
      break;
    case MEMENTO:
      response.setHeader(HttpHeaders.ALLOW, "GET,HEAD,OPTIONS");
      break;
    case RESOURCE:
      response.setHeader(HttpHeaders.ALLOW, "GET,HEAD,OPTIONS,PUT");
      break;
    }
  }

  static boolean contentTypeSupported(ContentType contentType) {
    String mime = contentType.getMimeType();
    if (RDFFormat.NQUADS.hasMIMEType(mime)) return false; // TODO: Support nquads
    RDFFormat fmt = RDFWriterRegistry.getInstance()
        .getFileFormatForMIMEType(mime);
    return fmt != null || RDFFormattable.LINK_FORMAT.equals(mime);
  }

  static ContentType contentType(String value) {
    if (value == null || value.isEmpty()) return ContentTypes.TEXT_TURTLE;
    if ("*/*".equals(value)) return ContentTypes.TEXT_TURTLE;
    if ("text/*".equals(value)) return ContentTypes.TEXT_TURTLE;
    if ("application/ld+json".equals(value)) return ContentType.APPLICATION_JSON;
    return ContentType.parse(value);
  }

  static ContentType contentType(HttpServletRequest request) {
    return contentType(request.getContentType());
  }

  static void ianaTypeStatements(HttpServletRequest request, URIImpl subject, BufferStatementsHandler buffer) {
    Collection<String> ianaTypes = requestLinkHeaders(request, "type");
    boolean containerImpl = false;
    boolean containerIxn = false;
    boolean resource = false;
    for (String ianaType: ianaTypes) {
      if (ianaType.equals(Vocabulary.CONTAINER.toString())){
        containerIxn = true;
        continue;
      }
      if (ianaType.equals(Vocabulary.BASIC_CONTAINER.toString())) containerImpl = true;
      if (ianaType.equals(Vocabulary.DIRECT_CONTAINER.toString())) containerImpl = true;
      if (ianaType.equals(Vocabulary.INDIRECT_CONTAINER.toString())) containerImpl = true;
      if (ianaType.equals(Vocabulary.RESOURCE.toString())) resource = true;
      buffer.handleStatement(new ContextStatementImpl(subject, cavendish.blazegraph.ldp.Vocabulary.IANA_TYPE, new URIImpl(ianaType), Vocabulary.INTERNAL_CONTEXT));
    }
    if (containerIxn && !containerImpl) {
      buffer.handleStatement(new ContextStatementImpl(subject, cavendish.blazegraph.ldp.Vocabulary.IANA_TYPE, Vocabulary.BASIC_CONTAINER, Vocabulary.INTERNAL_CONTEXT));
    }
    if (ianaTypes.isEmpty()) {
      buffer.handleStatement(new ContextStatementImpl(subject, cavendish.blazegraph.ldp.Vocabulary.IANA_TYPE, Vocabulary.RDF_SOURCE, Vocabulary.INTERNAL_CONTEXT));
    }
    if (!resource) {
      buffer.handleStatement(new ContextStatementImpl(subject, cavendish.blazegraph.ldp.Vocabulary.IANA_TYPE, Vocabulary.RESOURCE, Vocabulary.INTERNAL_CONTEXT));
    }
  }

  static String constraintsUri(HttpServletRequest request) {
    HttpURI httpUri = HttpURI.createHttpURI(request.getScheme(),request.getLocalName(),request.getLocalPort(),CONSTRAINTS_PATH,null,null,null);
    return httpUri.toString();
  }

  void responseLinkHeader(URI subject, HttpServletRequest request, HttpServletResponse response, long timestamp) throws Exception {
    addLinkHeaders(response, "type", ianaTypeStatements(subject, timestamp).map(stmt -> (URI)stmt.getObject()));
    URI timemap = timemapURI(subject);
    if (timemap != null) {
      addLinkHeaders(request, response, "timemap", timemap);
    }
  }

  static URI timemapURI(URI subject) {
    java.net.URI parsed = java.net.URI.create(subject.stringValue());
    if (parsed.getPath().startsWith(TIMEMAPS_PATH)) {
      return null; // no timemap versioning thank you very much
    } else {
      return new URIImpl(parsed.resolve(TIMEMAPS_PATH + parsed.getPath()).toString());
    }
  }

  static void addTypeLinkHeaders(HttpServletRequest request, HttpServletResponse response, URI... types) {
    addLinkHeaders(request, response, "type", types);
  }

  static void addLinkHeaders(HttpServletResponse response, String rel, Stream<URI> types) {
    ArrayList<URI> typeList = new ArrayList<>();
    types.forEach(type -> typeList.add(type));
    addLinkHeaders(null, response, rel, typeList.toArray(new URI[]{}));
  }

  static void addLinkHeaders(HttpServletRequest request, HttpServletResponse response, String rel, URI... types) {
    if (types.length == 0) return;
    for (URI type: types) {
      Link link = Link.fromUri(type.stringValue()).rel(rel).build();
      response.addHeader(HttpHeaders.LINK, link.toString());
      LOG.debug("added Link header: {}", link.toString());
    }
  }

  static Collection<String> requestLinkHeaders(HttpServletRequest request, String rel) {
    Enumeration<String> linkHeaders = request.getHeaders(HttpHeaders.LINK);
    Set<String> values = new HashSet<>();
    while (linkHeaders.hasMoreElements()) {
      Link link = Link.valueOf(linkHeaders.nextElement());
      if (link.getRels().contains(rel)) {
        values.add(link.getUri().toString());
      }
    }
    return values;
  }

  static ContentType responseType(HttpServletRequest request) {
    String value = request.getHeader(HttpHeaders.ACCEPT);

    if (value == null || value.isEmpty()) {
      return ContentTypes.TEXT_TURTLE;
    }
    List<String> accepts = AcceptParser.parseAcceptHeader(value);
    for (String accept:accepts){
      if (accept == null) continue;
      String [] values = accept.split(",");
      for (String hValue: values) {
        if (hValue == null || hValue.isEmpty()) continue;
        ContentType contentType = contentType(hValue);
        if (contentTypeSupported(contentType)) {
          return contentType;
        } else {
          LOG.warn("requested \"{}\" not supported", hValue);
        }
      }
    }
    throw new NotSupportedException("Unsupported content type requested: " + value);
  }

  static StringBuilder createContext(HttpServletRequest request) {
    StringBuilder b = new StringBuilder();
    b.append(request.getScheme()).append("://").append(request.getServerName());
    if ("http".equals(request.getScheme()) && 80 != request.getServerPort()) {
      b.append(':').append(Integer.toString(request.getServerPort()));
    }
    if ("https".equals(request.getScheme()) && 443 != request.getServerPort()) {
      b.append(':').append(Integer.toString(request.getServerPort()));
    }
    if (request.getContextPath() != null) b.append(request.getContextPath());
    return b;
  }

  protected String resourceToCreate(HttpServletRequest request, InteractionType containerType) {
    String resource = request.getPathInfo();
    String rootNode = rootNode(request);
    LOG.info("context = \"{}\", pathInfo = \"{}\"", request.getContextPath(), resource);
    if (rootNode.equals(resource)) {
      resource = resource + "/" + slug(request, containerType);
    } else {
      if (resource.endsWith("/")) resource = resource.substring(0, resource.length() - 1);
      resource = resource + "/" + slug(request, containerType);
    }
    StringBuilder b = createContext(request);
    b.append(resource);
    LOG.info("Requested creation of {}", b);

    return b.toString();
  }

  static String slug(HttpServletRequest request, InteractionType containerType) {
    String slug = null;
    if (containerType == InteractionType.TIMEMAP) {
      return Instant.now().toString();
    }

    if (request  != null) {
      slug = request.getHeader(LdpHeaders.SLUG);
    }
    if (slug == null || slug.isEmpty()) {
      slug = UUID.randomUUID().toString();
    }
    return slug;
  }
}
