package cavendish.blazegraph.rdf;

import java.util.concurrent.atomic.AtomicLong;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;

import cavendish.blazegraph.ldp.Vocabulary;

/**
 * Helper class adds statements to the sail as they are visited by a parser.
 */
public class AddStatementHandler extends RDFHandlerBase {

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
    public AddStatementHandler(final BigdataSailRepositoryConnection conn,
            final AtomicLong nmodified, final Resource... defaultContext) {
      if (conn.isReadOnly()) throw new IllegalArgumentException("connection cannot be read only");
      this.conn = conn;
      this.nmodified = nmodified;
      final boolean quads = conn.getTripleStore().isQuads();
      if (quads && defaultContext != null) {
          // The context may only be specified for quads.
          this.defaultContext = defaultContext; //new Resource[] { defaultContext };
      } else {
          this.defaultContext = new Resource[0];
      }
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

    	final Resource[] contexts = (Resource[]) 
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
        try {
          if (stmt.getPredicate().equals(Vocabulary.CONTAINS)) {
            if (stmt instanceof ContextStatementImpl && Vocabulary.INTERNAL_CONTEXT.equals(((ContextStatementImpl)stmt).getContext())) {
              conn.add(stmt,  contexts);
            } else {
              throw new ConstraintViolationException("PUT to update containment triples is not allowed");
            }
          } else {
            conn.add(stmt,  contexts);
          }
        } catch (RepositoryException e) {
          throw new RDFHandlerException(e);
        }

        if (contexts.length > 1) {
            // added to more than one context
            nmodified.addAndGet(contexts.length);
        } else {
            nmodified.incrementAndGet();
        }
    }
}