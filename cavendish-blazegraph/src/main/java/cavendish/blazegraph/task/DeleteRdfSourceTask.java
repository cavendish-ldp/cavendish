package cavendish.blazegraph.task;

import cavendish.blazegraph.ldp.Vocabulary;
import cavendish.blazegraph.rdf.BufferStatementsHandler;

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.task.AbstractApiTask;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class DeleteRdfSourceTask extends AbstractApiTask<Long> implements MutatingTask {

  private final URI subject;

  /**
   * @param namespace
   *            The namespace of the target KB instance.
   * @param timestamp
   *            The timestamp of the view of that KB instance.
   * @param isGRSRequired
   *            <code>true</code> iff the task requires a lock on the GRS
   *            index.
   */
  public DeleteRdfSourceTask(final String namespace, final boolean isGRSRequired,
      final URI subject) {
    super(namespace, -1, isGRSRequired); // setting a > 0 time makes view read-only
    this.subject = subject;
  }

  public Long call() throws Exception {
    final BigdataSailRepositoryConnection connection = this.getConnection();

    final BufferStatementsHandler buffer = new BufferStatementsHandler(); 
    final Resource[] c = new Resource[]{null}; // null indicates no context
    final boolean includeInferred = true; // default from BG
    connection.begin();

    try {

      // get statements with subject from the default context
      connection.exportStatements(this.subject, null, null, includeInferred, buffer, c);
      // get all statements from the subject context
      connection.export(buffer, new Resource[]{this.subject});
      // get all statements from the internal context
      connection.exportStatements(this.subject, null, null, includeInferred, buffer, Vocabulary.INTERNAL_CONTEXT);
      connection.exportStatements(null, Vocabulary.CONTAINS, subject, includeInferred, buffer, Vocabulary.INTERNAL_CONTEXT);
      connection.remove(buffer.statements());
      // tombstone the resource
      connection.add(this.subject, Vocabulary.IANA_TYPE, Vocabulary.DELETED, Vocabulary.INTERNAL_CONTEXT);
      return connection.commit2();
    } finally {
      if (connection != null) {
        if (connection.isActive()) connection.rollback();
        connection.close();
      }
    }
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }
}
