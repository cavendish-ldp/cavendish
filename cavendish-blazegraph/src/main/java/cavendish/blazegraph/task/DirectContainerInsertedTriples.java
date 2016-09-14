package cavendish.blazegraph.task;

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.sail.BigdataSailTupleQuery;

import java.util.Iterator;
import java.util.function.BiFunction;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;

public class DirectContainerInsertedTriples implements BiFunction<URI, BigdataSailRepositoryConnection, Iterator<Statement>>{

  private static final String SPARQL =
      "SELECT ?memberRelation ?member \n" +
      "WHERE { \n" +
      "  GRAPH <info:cavendish/> { ?container <http://www.iana.org/assignments/link-relations/type> <http://www.w3.org/ns/ldp#DirectContainer> } .\n" +
      "  ?container <http://www.w3.org/ns/ldp#membershipResource> <%s> .\n" +
      "  ?container <http://www.w3.org/ns/ldp#contains> ?member .\n" +
      "  ?container <http://www.w3.org/ns/ldp#hasMemberRelation> ?memberRelation" +
      "}";

  public Iterator<Statement> apply(URI subject, BigdataSailRepositoryConnection connection) {
    String sparql = SPARQL.replace("%s", subject.stringValue());
    try {
    BigdataSailTupleQuery parsedQuery =
        (BigdataSailTupleQuery) connection.prepareNativeSPARQLQuery(QueryLanguage.SPARQL, sparql, subject.stringValue());
    TupleQueryResult results = parsedQuery.evaluate();
    return new SyntheticStatementIterator(subject, results, "memberRelation", "member");
    } catch (Exception e) {
      throw new java.util.concurrent.CompletionException(e);
    }
  }
}
