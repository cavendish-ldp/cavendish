package cavendish.blazegraph.ldp;

import org.openrdf.model.URI;

import cavendish.ldp.api.RdfSource;

import com.bigdata.resources.IndexManager;

public class RdfSourceImpl extends ResourceImpl implements RdfSource {

  public RdfSourceImpl(URI uri, IndexManager index) {
		super(uri, index);
	}
}
