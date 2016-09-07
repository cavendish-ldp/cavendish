package cavendish.blazegraph.task;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;

import cavendish.blazegraph.ldp.Vocabulary;
import cavendish.blazegraph.rdf.AddStatementHandler;
import cavendish.blazegraph.rdf.BufferStatementsHandler;

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.task.AbstractApiTask;

import org.openrdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplaceRdfSourceTask extends AbstractApiTask<Long> implements MutatingTask {
  private static final Logger LOG = LoggerFactory.getLogger(ReplaceRdfSourceTask.class);
  private static boolean REPLACE_IANA_TYPE = false;
  private final URI subject;
  private final Iterator<Statement> statementIterator;
  /**
   * @param namespace
   *            The namespace of the target KB instance.
   * @param timestamp
   *            The timestamp of the view of that KB instance.
   * @param isGRSRequired
   *            <code>true</code> iff the task requires a lock on the GRS
   *            index.
   */
  public ReplaceRdfSourceTask(final String namespace, final boolean isGRSRequired,
      final URI subject) {
    this(namespace, isGRSRequired, subject, Collections.<Statement> emptyList());
  }

  public ReplaceRdfSourceTask(final String namespace, final boolean isGRSRequired,
      final URI subject, Collection<Statement> statements) {
    this(namespace, isGRSRequired, subject, statements.iterator());
  }

  public ReplaceRdfSourceTask(final String namespace, final boolean isGRSRequired,
      final URI subject, Iterator<Statement> statements) {
    super(namespace, -1, isGRSRequired); // setting a > 0 time makes view read-only
    this.subject = subject;
    this.statementIterator = statements;
  }

  public Long call() throws Exception {
    final AtomicLong nmodified = new AtomicLong(0L);
    final BigdataSailRepositoryConnection connection = this.getConnection();

    final RDFHandler handler = new AddStatementHandler(
        connection, nmodified);
    connection.begin();
    handler.startRDF();
    BufferStatementsHandler buffer = null; 
    final boolean includeInferred = true; // default from BG

    {
      Iterable<Statement> left;
      buffer = new BufferStatementsHandler(); 
      final Resource[] c = new Resource[]{null}; // null indicates no context

      // get statements with subject from the default context
      connection.exportStatements(subject, null, null, includeInferred, buffer, c);
      // get all statements from the subject context
      connection.exportStatements(null, null, null, includeInferred, buffer, new Resource[]{subject});
      // internal context triples should not be affected
      left = buffer.statements();
      connection.remove(left, new Resource[]{null, subject});
      for (Statement stmt: left) LOG.warn("replace deletes {}", stmt);
      buffer = null;
    }

    try {
      Statement stmt = null;
      boolean requiresType = true;
      while (this.statementIterator.hasNext()) {
        Statement next = this.statementIterator.next();
        Resource statementSubject = resolve(this.subject, next.getSubject());
        Resource context = next.getContext();
        if (!(this.subject.equals(statementSubject))) {
          context = this.subject;
        }
        if (!next.getSubject().equals(statementSubject) || context != next.getContext()) {
          next = new ContextStatementImpl(statementSubject, next.getPredicate(), next.getObject(), context);
        }

        if (next.getPredicate().equals(RDF.TYPE) && context == null) {
          requiresType = false;
        }
        //TODO check to make sure it's not the buffered IANA:type statements in internal context
        if (next.getContext() != null) {
          stmt = next;
        } else {
          stmt = new ContextStatementImpl(subject, next.getPredicate(), next.getObject(), context);
        }
        if (Vocabulary.INTERNAL_CONTEXT.equals(stmt.getContext()) && stmt.getPredicate().equals(Vocabulary.IANA_TYPE)) {
          if (buffer == null) buffer = new BufferStatementsHandler();
          buffer.handleStatement(stmt);
        } else {
          handler.handleStatement(stmt);
          LOG.warn("replace puts {}", stmt);
        }
      }

      if (requiresType) {
        stmt = new ContextStatementImpl(subject, RDF.TYPE, Vocabulary.RDF_SOURCE, null);
        LOG.error("inserting default rdf:type on replace: {}", stmt.toString());
        handler.handleStatement(stmt);
      }

      if (buffer != null && REPLACE_IANA_TYPE) {
        Iterable<Statement> left;
        left = buffer.statements();
        buffer = new BufferStatementsHandler();
        Resource[] internalContext = new Resource[]{Vocabulary.INTERNAL_CONTEXT};
        connection.exportStatements(subject, Vocabulary.IANA_TYPE, null, includeInferred, buffer, internalContext);
        connection.remove(buffer.statements(), internalContext);
        connection.add(left, internalContext);
      }
      handler.endRDF();
      return connection.commit2();
    } finally {
      if (connection.isActive()) connection.rollback();
      connection.close();
    }
  }

  public URI getInteractionModel() {
    return Vocabulary.RDF_SOURCE;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

}
