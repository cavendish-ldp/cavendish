package cavendish.blazegraph.rdf;

import java.util.concurrent.atomic.AtomicLong;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.sail.sparql.Bigdata2ASTSPARQLParser;

import cavendish.blazegraph.ldp.Vocabulary;

/**
 * Helper class adds statements to the sail as they are visited by a parser.
 */
public class UpdateStatementHandler extends RDFHandlerBase {

    private final BigdataSailRepositoryConnection conn;
    private final AtomicLong nmodified;
    private final Resource[] defaultContext;

    /**
     * 
     * @param conn
     * @param nmodified
     * @param defaultContexts
     * 			Only used if the statements themselves do not have a context.
     */
    public UpdateStatementHandler(final BigdataSailRepositoryConnection conn,
            final AtomicLong nmodified, final Resource... defaultContext) {
      if (conn.isReadOnly()) throw new IllegalArgumentException("connection cannot be read only");
      this.conn = conn;
      this.nmodified = nmodified;
      this.defaultContext = defaultContext;
      Bigdata2ASTSPARQLParser parser = new Bigdata2ASTSPARQLParser();
    }

    @Override
    public void startRDF() throws RDFHandlerException {
    }

    @Override
    public void endRDF() throws RDFHandlerException {
    }

    @Override
    public void handleStatement(final Statement stmt)
            throws RDFHandlerException {

    	final Resource[] c = (Resource[]) 
    			(stmt.getContext() == null 
    			?  defaultContext
                : new Resource[] { stmt.getContext() });
    	
  	    if (stmt.getPredicate().equals(RDF.TYPE)) {
          if (stmt.getObject().equals(RDF.BAG)) {
            throw new RDFHandlerException("LDP types of rdf:Bag disallowed");
          }
          if (stmt.getObject().equals(RDF.LIST)) {
            throw new RDFHandlerException("LDP types of rdf:List disallowed");
          }
  	      if (stmt.getObject().equals(RDF.SEQ)) {
  	        throw new RDFHandlerException("LDP types of rdf:Seq disallowed");
  	      }
  	    }
  	    if (stmt.getPredicate().equals(Vocabulary.CONTAINS)) {
  	      boolean detected = false;
  	      for (Resource context: c){
  	        detected = detected || context.equals(Vocabulary.INTERNAL_CONTEXT);
  	        if (detected) break;
  	      }
  	      if (!detected) {
  	        throw new ConstraintViolationException("PUT to update containment triples is not allowed");
  	      } else {
  	        //TODO add containment triples only to the internal context
  	      }
  	    }
        try {
            conn.add(stmt,  c);
        } catch (RepositoryException e) {

            throw new RDFHandlerException(e);

        }

        if (c.length >= 2) {
            // added to more than one context
            nmodified.addAndGet(c.length);
        } else {
            nmodified.incrementAndGet();
        }

    }

}