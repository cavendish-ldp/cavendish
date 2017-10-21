package cavendish.blazegraph.ldp;

import org.openrdf.model.URI;

import com.bigdata.resources.IndexManager;

import cavendish.ldp.api.Resource;

public class ResourceImpl implements Resource {
  protected URI uri;
  protected IndexManager index;

  public ResourceImpl(URI uri, IndexManager index) {
    initialize(uri, index);
  }

  protected void initialize(URI uri, IndexManager index) {
    this.uri = uri;
    this.index = index;
  }

}
