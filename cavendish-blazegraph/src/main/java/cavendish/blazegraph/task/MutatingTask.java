package cavendish.blazegraph.task;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public interface MutatingTask {

  byte LDP_RESOURCE_MASK = 1;
  byte LDP_RDFSOURCE_MASK = 2;
  byte LDP_CONTAINER_MASK = 4;
  byte LDP_BASIC_MASK = 8;
  byte LDP_DIRECT_MASK = 16;
  byte LDP_INDIRECT_MASK = 32;

  default URIImpl getContainer(URI subject) {
    String src = subject.stringValue();
    return new URIImpl(src.substring(0,src.lastIndexOf('/')));
  }

  default Resource resolve(Resource context, Resource resource) {
    java.net.URI parsed = java.net.URI.create(resource.stringValue());
    if (parsed.isAbsolute()) return resource;
    java.net.URI base = java.net.URI.create(context.stringValue());
    return new URIImpl(base.resolve(parsed).toString());
  }

}
