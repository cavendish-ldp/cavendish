package cavendish.blazegraph.task;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.ntriples.NTriplesWriterFactory;
import org.openrdf.sail.SailException;

import cavendish.blazegraph.rdf.writer.LinkFormatRdfWriterFactory;

public interface RDFFormattable {

  String LINK_FORMAT = "application/link-format";
  String APPLICATION_N_TRIPLES = "application/n-triples";

  static RDFFormat rdfFormat(Iterator<String> mimeTypes) {
    RDFFormat format = RDFFormat.TURTLE;
    if (mimeTypes!=null) {
      mimeTypesLoop:
        while(mimeTypes.hasNext()) {
          String types = mimeTypes.next();
          if (types == null) continue;
          for (String mt:types.split(",")) {
            mt = mt.trim();
            RDFFormat fmt = RDFWriterRegistry.getInstance()
                .getFileFormatForMIMEType(mt);
            if (fmt != null) format = fmt;
            break mimeTypesLoop;
          }
        }
    }
    return format;
  }

  static void registerTimeMapFormat() {
    RDFWriterRegistry.getInstance().add(new LinkFormatRdfWriterFactory());
  }

  static void registerApplicationNTriplesFormat() {
    RDFWriterRegistry instance = RDFWriterRegistry.getInstance();
    RDFFormat nt = RDFFormat.NTRIPLES;
    RDFFormat ntriples = new RDFFormat(APPLICATION_N_TRIPLES, Collections.singletonList(APPLICATION_N_TRIPLES),nt.getCharset(),
        nt.getFileExtensions(), nt.supportsNamespaces(), nt.supportsContexts());
    instance.add(new NTriplesWriterFactory(){
      @Override
      public RDFFormat getRDFFormat() {
        return ntriples;
      }
    });
  }

  static RDFFormat rdfFormat(String types) {
    for (String mt:types.split(",")) {
      mt = mt.trim();
      RDFFormat format = RDFWriterRegistry.getInstance()
          .getFileFormatForMIMEType(mt);
      if (format != null) return format;
    }
    return RDFFormat.TURTLE;
  }

  // client responsible for flush/close of out stream
  static RDFWriter getResponseWriter(final Iterator<String> mimeTypes,
      final OutputStream out) throws SailException, RepositoryException {
    RDFFormat format = rdfFormat(mimeTypes);
    return getResponseWriter(format, out);
  }

  // client responsible for flush/close of out stream
  static RDFWriter getResponseWriter(final String mimeTypes,
      final OutputStream out) throws SailException, RepositoryException {
    RDFFormat format = rdfFormat(mimeTypes);
    return getResponseWriter(format, out);
  }

  static RDFWriter getResponseWriter(final RDFFormat  format, final OutputStream out) {
    return RDFWriterRegistry.getInstance().get(format)
        .getWriter(out);
  }
}
