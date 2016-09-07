package cavendish.ldp.api;

import java.net.URI;

public interface Vocabulary {

  URI LDP = URI.create("http://www.w3.org/ns/ldp");
  URI BASIC_CONTAINER = LDP.resolve("#BasicContainer");
  URI CONSTRAINED_BY = LDP.resolve("#constrainedBy");
  URI CONTAINER = LDP.resolve("#Container");
  URI CONTAINS = LDP.resolve("#contains");
  URI DELETED = LDP.resolve("#DeletedResource");
  URI DIRECT_CONTAINER = LDP.resolve("#DirectContainer");
  URI HAS_MEMBER_RELATION = LDP.resolve("#hasMemberRelation");
  URI IANA_TYPE = URI.create("http://www.iana.org/assignments/link-relations/type");
  URI INDIRECT_CONTAINER = LDP.resolve("#IndirectContainer");
  URI INSERTED_CONTENT_RELATION = LDP.resolve("#insertedContentRelation");
  URI INTERNAL_CONTEXT = URI.create("info:cavendish/");
  URI MEMBERSHIP_RESOURCE = LDP.resolve("#membershipResource");
  URI RDF_SOURCE = LDP.resolve("#RDFSource");
  URI RESOURCE = LDP.resolve("#Resource");

  String DEFAULT_NS = "kb";
}
