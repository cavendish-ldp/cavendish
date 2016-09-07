package cavendish.blazegraph.task;

import java.util.stream.Stream;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import cavendish.blazegraph.ldp.Vocabulary;
import cavendish.blazegraph.rdf.BufferStatementsHandler;

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.task.AbstractApiTask;

import org.openrdf.model.Resource;

public class InteractionModelsQueryTask extends AbstractApiTask<Stream<Statement>> {

  private final URI subject;

  /**
   * @param namespace
   *            The namespace of the target KB instance.
   * @param timestamp
   *            The timestamp of the view of that KB instance.
   * @param isGSRRequired
   *            <code>true</code> iff the task requires a lock on the GRS
   *            index.
   */
  public InteractionModelsQueryTask(final String namespace, final long timestamp,
      final boolean isGRSRequired, final URI subject) {
    super(namespace, timestamp, isGRSRequired);
    this.subject = subject;
  }

  public Stream<Statement> call() throws Exception {
    final BigdataSailRepositoryConnection connection = getQueryConnection();

    final BufferStatementsHandler buffer = new BufferStatementsHandler(); 

    final boolean includeInferred = true; // default from BG

    try {

      // get statements with subject from the internal context
      connection.exportStatements(subject, Vocabulary.IANA_TYPE, null, includeInferred, buffer, new Resource[]{Vocabulary.INTERNAL_CONTEXT});

      return buffer.stream();
    } finally {
      if (connection != null) {
        connection.close();
      }
    }
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }
}
