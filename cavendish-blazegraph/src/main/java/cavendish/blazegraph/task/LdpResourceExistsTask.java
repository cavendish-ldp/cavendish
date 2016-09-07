package cavendish.blazegraph.task;

import java.util.Iterator;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import cavendish.blazegraph.ldp.Vocabulary;
import cavendish.blazegraph.rdf.BufferStatementsHandler;

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.task.AbstractApiTask;

public class LdpResourceExistsTask extends AbstractApiTask<Integer> {
  private final URI subject;
  public LdpResourceExistsTask(final String namespace, final long timestamp, final boolean isGRSRequired, final URI subject) {
    super(namespace, timestamp, isGRSRequired);
    this.subject = subject;
  }
  @Override
  public Integer call() throws Exception {
    int result = 200;
    final BufferStatementsHandler buffer = new BufferStatementsHandler();
    BigdataSailRepositoryConnection connection = null;
    try{
      connection = getConnection();
      connection.exportStatements(subject, Vocabulary.IANA_TYPE, null, isGRSRequired(), buffer, Vocabulary.INTERNAL_CONTEXT);
      Iterator<Statement> stmts = buffer.iterate();
      if (!stmts.hasNext()) result = 404;
      else if (stmts.next().getObject().toString().equals(Vocabulary.DELETED.toString())) result = 410;
    } finally {
      if (connection != null) connection.close();
    }
    return result;
  }
  @Override
  public boolean isReadOnly() {
    return true;
  }
}
