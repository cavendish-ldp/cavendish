package cavendish.blazegraph.task;

import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandler;

import cavendish.blazegraph.ldp.Vocabulary;
import cavendish.blazegraph.rdf.AddStatementHandler;
import cavendish.blazegraph.rdf.BufferStatementsHandler;

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.task.AbstractApiTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertMementoTask extends AbstractApiTask<Statement> implements MutatingTask {
  private static final Logger LOG = LoggerFactory.getLogger(InsertMementoTask.class);
  private final URI timemap;

  /**
   * @param namespace
   *            The namespace of the target KB instance.
   * @param timestamp
   *            The timestamp of the view of that KB instance.
   * @param isGRSRequired
   *            <code>true</code> iff the task requires a lock on the GRS
   *            index.
   */
  public InsertMementoTask(final String namespace, final boolean isGRSRequired,
      final URI timemap) {
    super(namespace, -1, isGRSRequired); // setting a > 0 time makes view read-only
    this.timemap = timemap;
  }

  public Statement call() throws Exception {
    final AtomicLong nmodified = new AtomicLong(0L);
    final BigdataSailRepositoryConnection connection = this.getConnection();
    final RDFHandler handler = new AddStatementHandler(
        connection, nmodified);
    Statement stmt = null;

    connection.begin();
    final BufferStatementsHandler buffer = new BufferStatementsHandler();
    connection.exportStatements(timemap, Vocabulary.IANA_ORIGINAL, null, true, buffer, Vocabulary.INTERNAL_CONTEXT);
    URI subject = (URI) buffer.enumerate().nextElement().getObject();
    java.net.URI parsed = java.net.URI.create(this.timemap.stringValue());
    String mementoTime = Instant.now().toString();
    // URI memento = new URIImpl(parsed.resolve(parsed.getPath() + "/" + java.net.URLEncoder.encode(Instant.now().toString(), "UTF8")).toString());
    URI memento = new URIImpl(parsed.resolve(parsed.getPath() + "/" + mementoTime).toString());
    handler.startRDF();
    try {
      SubjectStatementsQueryTask task = new SubjectStatementsQueryTask(Vocabulary.DEFAULT_NS, this.timestamp, false, subject, SubjectStatementsQueryTask.DEFAULT_PREFS);
      Iterator<Statement> statements = AbstractApiTask.submitApiTask(this.getIndexManager(), task).get();

      while (statements.hasNext()) {
        Statement next = statements.next();
        next = new ContextStatementImpl(next.getSubject(), next.getPredicate(), next.getObject(), memento);

        handler.handleStatement(next);
      }

      stmt = new ContextStatementImpl(memento, Vocabulary.IANA_TYPE, Vocabulary.MEMENTO_MEMENTO, Vocabulary.INTERNAL_CONTEXT);
      handler.handleStatement(stmt);
      stmt = new ContextStatementImpl(memento, Vocabulary.IANA_TYPE, Vocabulary.RDF_SOURCE, Vocabulary.INTERNAL_CONTEXT);
      handler.handleStatement(stmt);
      stmt = new ContextStatementImpl(memento, Vocabulary.IANA_TYPE, Vocabulary.RESOURCE, Vocabulary.INTERNAL_CONTEXT);
      handler.handleStatement(stmt);

      stmt = new ContextStatementImpl(timemap, Vocabulary.CONTAINS, memento, Vocabulary.INTERNAL_CONTEXT);
      LOG.info("inserting containment triple: {}", stmt.toString());
      handler.handleStatement(stmt);
      Statement result = new ContextStatementImpl(memento, Vocabulary.IANA_MEMENTO, subject, Vocabulary.INTERNAL_CONTEXT);
      handler.handleStatement(result);
      handler.endRDF();
      connection.commit2();
      return result;
    } finally {
      if (connection.isActive()) connection.rollback();
      connection.close();
    }
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

}
