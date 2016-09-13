package cavendish.blazegraph.task;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public interface MutatingTask {

  short LDP_RESOURCE_MASK = 1;
  short LDP_RDFSOURCE_MASK = 2;
  short LDP_CONTAINER_MASK = 4;
  short LDP_BASIC_MASK = 8;
  short LDP_DIRECT_MASK = 16;
  short LDP_INDIRECT_MASK = 32;
  short LDP_MEMBERSHIP_RESOURCE_MASK = 64;
  short LDP_MEMBERSHIP_RELATION_MASK = 128;
  short LDP_INSERTED_CONTENT_RELATION_MASK = 256;

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
