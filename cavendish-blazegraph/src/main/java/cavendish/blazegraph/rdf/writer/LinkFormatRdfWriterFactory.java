package cavendish.blazegraph.rdf.writer;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

import cavendish.blazegraph.task.RDFFormattable;

public class LinkFormatRdfWriterFactory implements RDFWriterFactory {

  static final RDFFormat LINK_FORMAT = new RDFFormat("Link Format", RDFFormattable.LINK_FORMAT, Charset.forName("UTF8"), "links", false, false);

  @Override
  public RDFFormat getRDFFormat() {
    return LINK_FORMAT;
  }

  @Override
  public RDFWriter getWriter(OutputStream out) {
    return new LinkFormatRDFWriter(out);
  }

  @Override
  public RDFWriter getWriter(Writer writer) {
    return new LinkFormatRDFWriter(writer);
  }

}
