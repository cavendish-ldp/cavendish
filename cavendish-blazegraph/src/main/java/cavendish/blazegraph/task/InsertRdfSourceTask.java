package cavendish.blazegraph.task;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import cavendish.blazegraph.ldp.Vocabulary;
import cavendish.blazegraph.rdf.AddStatementHandler;
import cavendish.blazegraph.rdf.ConstraintViolationException;

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.task.AbstractApiTask;

import org.openrdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertRdfSourceTask extends AbstractApiTask<Long> implements MutatingTask {
  private static final Logger LOG = LoggerFactory.getLogger(InsertRdfSourceTask.class);
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
  public InsertRdfSourceTask(final String namespace, final boolean isGRSRequired,
      final URI subject) {
    this(namespace, isGRSRequired, subject, Collections.<Statement> emptyList());
  }

  public InsertRdfSourceTask(final String namespace, final boolean isGRSRequired,
      final URI subject, Collection<Statement> statements) {
    this(namespace, isGRSRequired, subject, statements.iterator());
  }

  public InsertRdfSourceTask(final String namespace, final boolean isGRSRequired,
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
    Statement stmt = null;

    connection.begin();
    handler.startRDF();
    try {
      boolean requiresType = true;
      short ldpClassSwitches = 0;
      while (this.statementIterator.hasNext()) {
        Statement next = this.statementIterator.next();
        Resource statementSubject = resolve(this.subject, next.getSubject());
        Value statementObject = (next.getObject() instanceof URI) ? resolve(this.subject, (Resource) next.getObject()) : next.getObject();
        Resource context = next.getContext();
        if (!(this.subject.equals(statementSubject))) {
          context = this.subject;
        }
        if (!(statementObject.equals(next.getObject()))) {
          next = new ContextStatementImpl(next.getSubject(), next.getPredicate(), statementObject, next.getContext());
        }
        if (!next.getSubject().equals(statementSubject) || context != next.getContext()) {
          next = new ContextStatementImpl(statementSubject, next.getPredicate(), statementObject, context);
        }

        if (next.getPredicate().equals(RDF.TYPE) && context == null) {
          requiresType = false;
        }
        if (next.getPredicate().equals(Vocabulary.IANA_TYPE) && Vocabulary.INTERNAL_CONTEXT.equals(next.getContext())) {
          if (Vocabulary.BASIC_CONTAINER.equals(next.getObject())) ldpClassSwitches |= LDP_BASIC_MASK;
          if (Vocabulary.DIRECT_CONTAINER.equals(next.getObject())) ldpClassSwitches |= LDP_DIRECT_MASK;
          if (Vocabulary.INDIRECT_CONTAINER.equals(next.getObject())) ldpClassSwitches |= LDP_INDIRECT_MASK;
          if (Vocabulary.RESOURCE.equals(next.getObject())) ldpClassSwitches |= LDP_RESOURCE_MASK;
          if (Vocabulary.RDF_SOURCE.equals(next.getObject())) ldpClassSwitches |= LDP_RDFSOURCE_MASK;
        }
        if (next.getPredicate().equals(Vocabulary.MEMBERSHIP_RESOURCE)) {
          ldpClassSwitches |= LDP_MEMBERSHIP_RESOURCE_MASK;
        }
        if (next.getPredicate().equals(Vocabulary.INSERTED_CONTENT_RELATION)) {
          ldpClassSwitches |= LDP_INSERTED_CONTENT_RELATION_MASK;
        }
        //TODO check to make sure internal context only
        if (next.getContext() != null) {
          stmt = next;
        } else {
          stmt = new ContextStatementImpl(statementSubject, next.getPredicate(), next.getObject(), context);
        }
        handler.handleStatement(stmt);
      }
      if (requiresType) {
        stmt = new ContextStatementImpl(subject, RDF.TYPE, Vocabulary.RDF_SOURCE, null);
        LOG.warn("inserting default rdf:type on create: {}", stmt.toString());
        handler.handleStatement(stmt);
      }
      if (ldpClassSwitches == 0) {
        stmt = new ContextStatementImpl(subject, Vocabulary.IANA_TYPE, Vocabulary.RDF_SOURCE, Vocabulary.INTERNAL_CONTEXT);
        LOG.warn("inserting default iana:type on create: {}", stmt.toString());
        handler.handleStatement(stmt);
        ldpClassSwitches |= LDP_RDFSOURCE_MASK;
      }
      if ((ldpClassSwitches & LDP_RESOURCE_MASK) != LDP_RESOURCE_MASK) {
        stmt = new ContextStatementImpl(subject, Vocabulary.IANA_TYPE, Vocabulary.RESOURCE, Vocabulary.INTERNAL_CONTEXT);
        LOG.warn("inserting default iana:type on create: {}", stmt.toString());
        handler.handleStatement(stmt);
        ldpClassSwitches |= LDP_RESOURCE_MASK;
      }
      stmt = new ContextStatementImpl(getContainer(subject), Vocabulary.CONTAINS, subject, Vocabulary.INTERNAL_CONTEXT);
      LOG.info("inserting containment triple: {}", stmt.toString());
      handler.handleStatement(stmt);
      validateContainerModel(subject, ldpClassSwitches);
      addTimeMap(subject, handler);
      handler.endRDF();
      return connection.commit2();
    } finally {
      if (connection.isActive()) connection.rollback();
      connection.close();
    }
  }

  public void validateContainerModel(URI subject, short ldpClassSwitches) throws ConstraintViolationException {
    // no validations for Basic
    if ((ldpClassSwitches & LDP_BASIC_MASK) == LDP_BASIC_MASK) return;
    if ((ldpClassSwitches & LDP_DIRECT_MASK) == LDP_DIRECT_MASK) {
      if ((ldpClassSwitches & LDP_MEMBERSHIP_RESOURCE_MASK) != LDP_MEMBERSHIP_RESOURCE_MASK) {
        throw new ConstraintViolationException("non-BasicContainer must have a ldp:membershipResource");
      }
      return;
    };
    if ((ldpClassSwitches & LDP_INDIRECT_MASK) == LDP_INDIRECT_MASK) {
      if ((ldpClassSwitches & LDP_INSERTED_CONTENT_RELATION_MASK) != LDP_INSERTED_CONTENT_RELATION_MASK) {
        throw new ConstraintViolationException("IndirectContainer must have a ldp:insertedContentRelation");
      }
      return;
    }
  }

  public static void addTimeMap(URI subject, RDFHandler handler) throws RDFHandlerException {
    java.net.URI parsed = java.net.URI.create(subject.stringValue());
    parsed = parsed.resolve("/timemaps" + parsed.getPath());
    URIImpl timemap = new URIImpl(parsed.toString());
    handler.handleStatement(new ContextStatementImpl(subject, Vocabulary.IANA_TIMEMAP, timemap, Vocabulary.INTERNAL_CONTEXT));
    handler.handleStatement(new ContextStatementImpl(timemap, Vocabulary.IANA_ORIGINAL, subject, Vocabulary.INTERNAL_CONTEXT));
    handler.handleStatement(new ContextStatementImpl(timemap, Vocabulary.IANA_TYPE, Vocabulary.DIRECT_CONTAINER, Vocabulary.INTERNAL_CONTEXT));
    handler.handleStatement(new ContextStatementImpl(timemap, Vocabulary.IANA_TYPE, Vocabulary.MEMENTO_TIMEMAP, Vocabulary.INTERNAL_CONTEXT));
    handler.handleStatement(new ContextStatementImpl(timemap, Vocabulary.MEMBERSHIP_RESOURCE, subject, null));
    handler.handleStatement(new ContextStatementImpl(timemap, Vocabulary.INSERTED_CONTENT_RELATION, timemap, null));
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

}
