package cavendish.blazegraph.ldp;

import org.openrdf.model.URI;

import cavendish.ldp.api.RdfSource;

import com.bigdata.resources.IndexManager;

public class RdfSourceImpl implements RdfSource {

  private URI uri;
  private IndexManager index;

  public RdfSourceImpl(URI uri, IndexManager index) {
		initialize(uri, index);
	}

	protected void initialize(URI uri, IndexManager index) {
    this.uri = uri;
    this.index = index;
	}
}
