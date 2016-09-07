package cavendish.blazegraph.task;

import java.util.Iterator;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

public class SyntheticStatementIterator implements Iterator<Statement> {

  private final URI subject;
  private final TupleQueryResult results;
  private final String predicateBinding, objectBinding;
  public SyntheticStatementIterator(URI subject, TupleQueryResult results, String predicateBinding, String objectBinding) {
    this.subject = subject;
    this.results = results;
    this.predicateBinding = predicateBinding;
    this.objectBinding = objectBinding;
  }

  @Override
  public boolean hasNext() {
    try {
      return results.hasNext();
    } catch (QueryEvaluationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Statement next() {
    BindingSet tuple;
    try {
      tuple = results.next();
      Statement next = new StatementImpl(this.subject, new URIImpl(tuple.getBinding(predicateBinding).getValue().stringValue()),
          new URIImpl(tuple.getBinding(objectBinding).getValue().stringValue()));
      return next;
    } catch (QueryEvaluationException e) {
      throw new RuntimeException(e);
    }
  }

}
