package cavendish.blazegraph.rdf.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;

import cavendish.blazegraph.ldp.Vocabulary;

public class LinkFormatRDFWriter implements RDFWriter {

  private static final String[] STRING_ARRAY_TYPE = new String[0];

  private static final String FIRST_MEMENTO = "memento first";
  private static final String FIRST_AND_LAST_MEMENTO = "memento first last";
  private static final String LAST_MEMENTO = "memento last";
  private static final String MEMENTO = "memento";
  private static final char[] NEWLINE_JOIN = new char[]{',','\n'};

  private WriterConfig config;

  private Map<String, URI> mementos = null;

  private URI originalURI = null;
  private URI timemapURI = null;

  private Writer out = null;

  public LinkFormatRDFWriter(OutputStream out) {
    setOutput(out);  
  }

  public LinkFormatRDFWriter(Writer out) {
    setOutput(out);
  }

  public void setOutput(OutputStream out) {
    setOutput(new OutputStreamWriter(out, Charset.forName("UTF-8")));
  }

  public void setOutput(Writer out) {
    this.out = out;
  }

  @Override
  public void startRDF() throws RDFHandlerException {
    resetState();
  }

  @Override
  public void endRDF() throws RDFHandlerException {
    String[] timestamps = this.mementos.keySet().toArray(STRING_ARRAY_TYPE);
    Arrays.sort(timestamps);
    try {
      if (this.originalURI != null) {
        writeLink(this.originalURI, this.out, "original");
        this.out.write(NEWLINE_JOIN);
      }

      if (timestamps.length > 0) {
        writeLink(this.timemapURI, this.out,
            "rel", "self", "type", "application/link-format", "from", timestamps[0], "until", timestamps[timestamps.length - 1]);
      } else {
        writeLink(this.timemapURI, this.out,
            "rel", "self", "type", "application/link-format");
      }

      for(int i=0;i<timestamps.length;i++) {
        this.out.write(NEWLINE_JOIN);
        boolean first = (i == 0);
        boolean last = (i == timestamps.length - 1);
        String rel = (first && last) ? FIRST_AND_LAST_MEMENTO : (first ? FIRST_MEMENTO : (last ? LAST_MEMENTO : MEMENTO));

        /** 
         * <http://arxiv.example.net/web/20000620180259/http://a.example.org>
         * ; rel="first memento";datetime="Tue, 20 Jun 2000 18:02:59 GMT"
         */
        writeLink(this.mementos.get(timestamps[i]), this.out, "rel", rel, "datetime", timestamps[i]);
      }
      this.out.flush();
    } catch (IOException e) {
      throw new RDFHandlerException(e);
    }
    resetState();
  }

  private void resetState() {
    if (this.mementos == null) {
      this.mementos = new HashMap<>();
    } else {
      this.mementos.clear();
    }
    this.timemapURI = null;
  }

  @Override
  public void handleNamespace(String prefix, String uri)
      throws RDFHandlerException {

  }

  @Override
  public void handleStatement(Statement st) throws RDFHandlerException {
    if (st.getPredicate().equals(Vocabulary.CONTAINS)) {
      if (this.timemapURI == null) this.timemapURI = (URI)st.getSubject();
      URI object = (URI) st.getObject();
      this.mementos.put(object.getLocalName(), object);
    }
    if (st.getPredicate().equals(Vocabulary.IANA_ORIGINAL)) {
      if (this.timemapURI == null) this.timemapURI = (URI)st.getSubject();
      this.originalURI = (URI)st.getObject();
    }
  }

  static void writeLink(URI uriParam, Writer out, String... params ) throws IOException {
    out.write('<');
    out.write(uriParam.stringValue());
    out.write('>');
    if (params.length > 0) {
      for (int i=0;i<params.length;i+=2) {
        boolean pair = i + 1 < params.length;
        String param = pair ? params[i] : "rel";
        String value = pair ? params[i+1] : params[i];
        out.write(';');
        out.write(' ');
        out.write(param);
        out.write("=\"");
        out.write(value);
        out.write('"');
      }
    }
  }

  @Override
  public void handleComment(String comment) throws RDFHandlerException {

  }

  @Override
  public RDFFormat getRDFFormat() {
    return LinkFormatRdfWriterFactory.LINK_FORMAT;
  }

  @Override
  public void setWriterConfig(WriterConfig config) {
    this.config = config;
  }

  @Override
  public WriterConfig getWriterConfig() {
    return config;
  }

  @Override
  public Collection<RioSetting<?>> getSupportedSettings() {
    // TODO Auto-generated method stub
    return null;
  }

}
