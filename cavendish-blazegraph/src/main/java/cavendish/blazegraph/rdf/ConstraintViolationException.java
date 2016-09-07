package cavendish.blazegraph.rdf;

import org.openrdf.rio.RDFHandlerException;

public class ConstraintViolationException extends RDFHandlerException {

  private static final long serialVersionUID = 9066523647322797644L;

  public ConstraintViolationException(String msg) {
    super(msg);
  }

  public ConstraintViolationException(Throwable cause) {
    super(cause);
  }

  public ConstraintViolationException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
