package cavendish.blazegraph.ldp;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public interface Vocabulary {
  URI BASIC_CONTAINER = new URIImpl(cavendish.ldp.api.Vocabulary.BASIC_CONTAINER.toString());
  String CONSTRAINED_BY = cavendish.ldp.api.Vocabulary.CONSTRAINED_BY.toString();
  URI CONTAINER = new URIImpl(cavendish.ldp.api.Vocabulary.CONTAINER.toString());
  URI CONTAINS = new URIImpl(cavendish.ldp.api.Vocabulary.CONTAINS.toString());
  URI DELETED = new URIImpl(cavendish.ldp.api.Vocabulary.DELETED.toString());
  URI DIRECT_CONTAINER = new URIImpl(cavendish.ldp.api.Vocabulary.DIRECT_CONTAINER.toString());
  URI IANA_MEMENTO = new URIImpl(cavendish.ldp.api.Vocabulary.IANA_MEMENTO.toString());
  URI IANA_ORIGINAL = new URIImpl(cavendish.ldp.api.Vocabulary.IANA_ORIGINAL.toString());
  URI IANA_TIMEMAP = new URIImpl(cavendish.ldp.api.Vocabulary.IANA_TIMEMAP.toString());
  URI IANA_TYPE = new URIImpl(cavendish.ldp.api.Vocabulary.IANA_TYPE.toString());
  URI INDIRECT_CONTAINER = new URIImpl(cavendish.ldp.api.Vocabulary.INDIRECT_CONTAINER.toString());
  URI INSERTED_CONTENT_RELATION = new URIImpl(cavendish.ldp.api.Vocabulary.INSERTED_CONTENT_RELATION.toString());
  URI INTERNAL_CONTEXT = new URIImpl(cavendish.ldp.api.Vocabulary.INTERNAL_CONTEXT.toString());
  URI MEMBERSHIP_RESOURCE = new URIImpl(cavendish.ldp.api.Vocabulary.MEMBERSHIP_RESOURCE.toString());
  URI MEMENTO_MEMENTO = new URIImpl(cavendish.ldp.api.Vocabulary.MEMENTO_MEMENTO.toString());
  URI MEMENTO_TIMEMAP = new URIImpl(cavendish.ldp.api.Vocabulary.MEMENTO_TIMEMAP.toString());
  URI NON_RDF_SOURCE = new URIImpl(cavendish.ldp.api.Vocabulary.NON_RDF_SOURCE.toString());
  URI RDF_SOURCE = new URIImpl(cavendish.ldp.api.Vocabulary.RDF_SOURCE.toString());
  URI RESOURCE = new URIImpl(cavendish.ldp.api.Vocabulary.RESOURCE.toString());
  String DEFAULT_NS = cavendish.ldp.api.Vocabulary.DEFAULT_NS;
  int SC_PRECONDITION_REQUIRED = 428;
}
