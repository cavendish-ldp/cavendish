package cavendish.ldp.api;

import java.net.URI;

public interface Vocabulary {

  URI IANA_NS = URI.create("http://www.iana.org/assignments/relation/");
  URI LDP_NS = URI.create("http://www.w3.org/ns/ldp");
  URI MEMENTO_NS = URI.create("https://mementoweb.org/");
  URI HTTP_HEADERS_NS = URI.create("http://www.w3.org/2011/http-headers");

  URI BASIC_CONTAINER = LDP_NS.resolve("#BasicContainer");
  URI CONSTRAINED_BY = LDP_NS.resolve("#constrainedBy");
  URI CONTAINER = LDP_NS.resolve("#Container");
  URI CONTAINS = LDP_NS.resolve("#contains");
  URI DELETED = LDP_NS.resolve("#DeletedResource");
  URI DIRECT_CONTAINER = LDP_NS.resolve("#DirectContainer");
  URI HAS_MEMBER_RELATION = LDP_NS.resolve("#hasMemberRelation");
  URI HTTP_CONTENT_LENGTH = HTTP_HEADERS_NS.resolve("#content-length");
  URI HTTP_CONTENT_TYPE = HTTP_HEADERS_NS.resolve("#content-type");
  URI HTTP_LAST_MODIFIED = HTTP_HEADERS_NS.resolve("#last-modified");
  URI IANA_CANONICAL = IANA_NS.resolve("canonical");
  URI IANA_MEMENTO = IANA_NS.resolve("memento");
  URI IANA_ORIGINAL = IANA_NS.resolve("original");
  URI IANA_TIMEMAP = IANA_NS.resolve("timemap");
  URI IANA_TYPE = IANA_NS.resolve("type");
  URI INDIRECT_CONTAINER = LDP_NS.resolve("#IndirectContainer");
  URI INSERTED_CONTENT_RELATION = LDP_NS.resolve("#insertedContentRelation");
  URI INTERNAL_CONTEXT = URI.create("info:cavendish/");
  URI INTERNAL_CONTENT_LOCATION = INTERNAL_CONTEXT.resolve("contentLocation");
  URI MEMBERSHIP_RESOURCE = LDP_NS.resolve("#membershipResource");
  URI MEMENTO_MEMENTO = MEMENTO_NS.resolve("Memento");
  URI MEMENTO_TIMEMAP = MEMENTO_NS.resolve("TimeMap");
  URI NON_RDF_SOURCE = LDP_NS.resolve("#NonRDFSource");
  URI RDF_SOURCE = LDP_NS.resolve("#RDFSource");
  URI RESOURCE = LDP_NS.resolve("#Resource");

  String DEFAULT_NS = "kb";
  static enum InteractionType {
    RESOURCE, RDFSOURCE, MEMENTO, CONTAINER, TIMEMAP, NONRDFSOURCE
  }
}
