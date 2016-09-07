package cavendish.blazegraph.task;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.UpdateExpr;

import cavendish.blazegraph.ldp.Vocabulary;
import cavendish.blazegraph.rdf.UpdateVisitor;

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.task.AbstractApiTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateRdfSourceTask extends AbstractApiTask<Long> {
  private static final Logger LOG = LoggerFactory.getLogger(UpdateRdfSourceTask.class);
	private final URI subject;
	private final Iterator<UpdateExpr> updateIterator;
	 /**
     * @param namespace
     *            The namespace of the target KB instance.
     * @param timestamp
     *            The timestamp of the view of that KB instance.
     * @param isGRSRequired
     *            <code>true</code> iff the task requires a lock on the GRS
     *            index.
     */
	public UpdateRdfSourceTask(final String namespace, final boolean isGRSRequired,
	    final URI subject) {
		this(namespace, isGRSRequired, subject, Collections.<UpdateExpr> emptyList());
	}

	public UpdateRdfSourceTask(final String namespace, final boolean isGRSRequired,
		  final URI subject, List<UpdateExpr> updates) {
		this(namespace, isGRSRequired, subject, updates.iterator());
	}

	public UpdateRdfSourceTask(final String namespace, final boolean isGRSRequired,
		  final URI subject, Iterator<UpdateExpr> updates) {
		super(namespace, 0, isGRSRequired); // setting a > 0 time makes view read-only
		this.subject = subject;
		this.updateIterator = updates;
	}

	public Long call() throws Exception {
		final AtomicLong nmodified = new AtomicLong(0L);
		final BigdataSailRepositoryConnection connection = this.getConnection();
		connection.begin();
		URI subject = new URIImpl(this.subject.toString());
		try {
  		while (this.updateIterator.hasNext()) {
  		  UpdateExpr next = this.updateIterator.next();
  		  next.visit(new UpdateVisitor());
  		}
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

	private URIImpl getContainer(URI subject) {
	  String src = subject.stringValue();
	  return new URIImpl(src.substring(0,src.lastIndexOf('/')));
	}
}
