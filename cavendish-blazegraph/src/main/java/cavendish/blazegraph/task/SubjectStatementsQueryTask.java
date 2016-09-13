package cavendish.blazegraph.task;

import java.util.Iterator;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import cavendish.blazegraph.ldp.Vocabulary;
import cavendish.blazegraph.rdf.BufferStatementsHandler;
import cavendish.ldp.api.SerializationPreference;

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.task.AbstractApiTask;

import org.apache.commons.collections.iterators.IteratorChain;
import org.openrdf.model.Resource;

public class SubjectStatementsQueryTask extends AbstractApiTask<Iterator<Statement>> {
  public static final SerializationPreference DEFAULT_PREFS = new DefaultSerializationPreference();
  private final URI subject;
  private final SerializationPreference prefs;

  /**
   * @param namespace
   *            The namespace of the target KB instance.
   * @param timestamp
   *            The timestamp of the view of that KB instance.
   * @param isGSRRequired
   *            <code>true</code> iff the task requires a lock on the GRS
   *            index.
   */
  public SubjectStatementsQueryTask(final String namespace, final long timestamp,
      final boolean isGRSRequired, final URI subject) {
    this(namespace, timestamp, isGRSRequired, subject, new DefaultSerializationPreference());
  }

  public SubjectStatementsQueryTask(final String namespace, final long timestamp,
      final boolean isGRSRequired, final URI subject, SerializationPreference prefs) {
    super(namespace, timestamp, isGRSRequired);
    this.subject = subject;
    this.prefs = prefs;
  }

  @SuppressWarnings("unchecked")
  public Iterator<Statement> call() throws Exception {
    final BigdataSailRepositoryConnection connection = getQueryConnection();

    final BufferStatementsHandler buffer = new BufferStatementsHandler(); 

    final boolean includeInferred = true; // default from BG

    try {

      // get statements with subject from the default context
      connection.exportStatements(subject, null, null, includeInferred, buffer, new Resource[]{null});
      // get all statements from the subject context
      connection.exportStatements(null, null, null, includeInferred, buffer, new Resource[]{subject});
      boolean preferMinimal = this.prefs.includeMinimalContainer(this.prefs.preferMinimal());
      boolean explicitContains = this.prefs.includeContainment(false);
      boolean implicitContains = this.prefs.includeContainment(true) && !this.prefs.omitContainment(false);
      boolean explicitMembers = this.prefs.includeMembership(false);
      boolean implicitMembers = this.prefs.includeMembership(true) && !this.prefs.omitMembership(false);
      if (explicitContains || (implicitContains && !preferMinimal)) {
        // get containment statements for subject from the default context
        connection.exportStatements(subject, Vocabulary.CONTAINS, null, includeInferred, buffer, Vocabulary.INTERNAL_CONTEXT);
        if (explicitContains) this.prefs.acknowledge(true);
      }
      if (explicitMembers || (implicitMembers && !preferMinimal)) {
        Iterator<Statement> directSynthetic = new DirectContainerInsertedTriples().apply(subject, connection);
        Iterator<Statement> indirectSynthetic = new IndirectContainerInsertedTriples().apply(subject, connection);
        if (explicitMembers) this.prefs.acknowledge(true);
        return new IteratorChain(new IteratorChain(buffer.iterate(), directSynthetic), indirectSynthetic);
      } else {
        if (preferMinimal) this.prefs.acknowledge(true);
        return buffer.iterate();
      }
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
  static class DefaultSerializationPreference implements SerializationPreference {

    @Override
    public boolean preferMinimal() {
      return false;
    }

    @Override
    public boolean includeContainment(boolean defaultValue) {
      return true;
    }

    @Override
    public boolean includeMembership(boolean defaultValue) {
      return true;
    }

    @Override
    public boolean includeMinimalContainer(boolean defaultValue) {
      return true;
    }

    @Override
    public void acknowledge(boolean value) {      
    }

    @Override
    public boolean wasAcknowledged() {
      return false;
    }

    @Override
    public boolean omitContainment(boolean defaultValue) {
      return false;
    }

    @Override
    public boolean omitMembership(boolean defaultValue) {
      return false;
    }

  }
}
