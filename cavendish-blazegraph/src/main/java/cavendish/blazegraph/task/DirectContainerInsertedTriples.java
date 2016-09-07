package cavendish.blazegraph.task;

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.sail.BigdataSailTupleQuery;

import java.util.Iterator;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;

public abstract class DirectContainerInsertedTriples {

  private static final String SPARQL =
      "SELECT ?memberRelation ?member \n" +
      "WHERE { \n" +
      "  ?directContainer <http://www.w3.org/ns/ldp#membershipResource> <%s> .\n" +
      "  ?directContainer <http://www.w3.org/ns/ldp#contains> ?member .\n" +
      "  ?directContainer <http://www.w3.org/ns/ldp#hasMemberRelation> ?memberRelation" +
      "}";

  public static Iterator<Statement> call(URI subject, BigdataSailRepositoryConnection connection) throws Exception {
    String sparql = SPARQL.replace("%s", subject.stringValue());
    BigdataSailTupleQuery parsedQuery =
        (BigdataSailTupleQuery) connection.prepareNativeSPARQLQuery(QueryLanguage.SPARQL, sparql, subject.stringValue());
    TupleQueryResult results = parsedQuery.evaluate();
    return new SyntheticStatementIterator(subject, results, "memberRelation", "member");
  }
}
