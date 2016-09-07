package cavendish.blazegraph.rdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.openrdf.model.Statement;
import org.openrdf.rio.helpers.RDFHandlerBase;

public class BufferStatementsHandler extends RDFHandlerBase {
	final List<Statement> statements = new ArrayList<Statement>();

	@Override
    public void handleStatement(final Statement stmt) {
		  statements.add(stmt);
    }

	public Iterator<Statement> iterate() {
    return statements.iterator();	
  }

  public Enumeration<Statement> enumerate() {
    return Collections.enumeration(statements);
  }

	public Stream<Statement> stream() {
	  return statements.stream();
	}

	public Iterable<Statement> statements() {
	  return Collections.unmodifiableList(statements);
	}
}
