package cavendish.blazegraph.ldp;

import org.openrdf.model.URI;

import com.bigdata.resources.IndexManager;

import cavendish.ldp.api.NonRdfSource;
import cavendish.ldp.api.RdfSource;

public class NonRdfSourceImpl extends ResourceImpl implements NonRdfSource {

  public NonRdfSourceImpl(URI uri, IndexManager index, URI contentLocation) {
    super(uri, index);
  }

  @Override
  public RdfSource getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isRemote() {
    // TODO Support request with Location header per Fedora spec
    return true;
  }

  @Override
  public java.net.URI getContentLocation() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setContentLocation(URI contentLocation) {
    
  }
}
