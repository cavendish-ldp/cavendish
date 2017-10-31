package cavendish.blazegraph.task;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bigdata.rdf.task.AbstractApiTask;

import cavendish.blazegraph.ldp.Vocabulary;

public abstract class InsertResourceTask extends AbstractApiTask<Long> implements MutatingTask {
	  private static final Logger LOG = LoggerFactory.getLogger(InsertResourceTask.class);
	  protected final URI subject;
	  protected final Iterator<Statement> statementIterator;
	  /**
	   * @param namespace
	   *            The namespace of the target KB instance.
	   * @param timestamp
	   *            The timestamp of the view of that KB instance.
	   * @param isGRSRequired
	   *            <code>true</code> iff the task requires a lock on the GRS
	   *            index.
	   */
	  public InsertResourceTask(final String namespace, final boolean isGRSRequired,
	      final URI subject) {
	    this(namespace, isGRSRequired, subject, Collections.<Statement> emptyList());
	  }

	  public InsertResourceTask(final String namespace, final boolean isGRSRequired,
	      final URI subject, Collection<Statement> statements) {
	    this(namespace, isGRSRequired, subject, statements.iterator());
	  }

	  public InsertResourceTask(final String namespace, final boolean isGRSRequired,
	      final URI subject, Iterator<Statement> statements) {
	    super(namespace, -1, isGRSRequired); // setting a > 0 time makes view read-only
	    this.subject = subject;
	    this.statementIterator = statements;
	  }

	  public static void addTimeMap(URI subject, RDFHandler handler) throws RDFHandlerException {
		  java.net.URI parsed = java.net.URI.create(subject.stringValue());
		  parsed = parsed.resolve("/timemaps" + parsed.getPath());
		  LOG.info("creating LDPCv/TimeMap at %s", parsed);
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
