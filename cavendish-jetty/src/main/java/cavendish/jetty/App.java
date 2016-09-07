package cavendish.jetty;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.openrdf.repository.Repository;

import com.bigdata.journal.IIndexManager;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.sail.webapp.NanoSparqlServer;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.util.config.NicUtil;

public class App implements Runnable
{
  private static final Logger log = Logger
      .getLogger(App.class);

  private int port;
  private final IIndexManager indexManager;
  private final Map<String, String> initParams;
  private final Server server;
  public App(int port, IIndexManager indexManager, Map<String, String> initParams) {
    this.port = port;
    this.indexManager = indexManager;
    this.initParams = initParams;

    try {
      server = NanoSparqlServer.newInstance(this.port, this.indexManager,
          this.initParams);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
    }
  }

  public int port()
  {
    return this.port;
  }

  public void run()
  {
    try {
      server.start();
      NanoSparqlServer.awaitServerStart(server);
      port = ((ServerConnector)server.getConnectors()[0])
          .getLocalPort();

      boolean loopbackOk = true;
      String hostAddr = NicUtil.getIpAddress("default.nic",
          "default", loopbackOk);

      if (hostAddr == null) {

        hostAddr = "localhost";

      }

      final String serviceURL = new URL("http", hostAddr, port, ""/* file */)
          .toExternalForm();

      System.out.println("serviceURL: " + serviceURL);
      server.join();
    } catch (Throwable t) {
      stop();
      log.error(t, t);
    }

  }

  public void stop() {
    try {
      server.stop();
      server.destroy();
    } catch (Throwable t) {
      log.error(t, t);
    }
  }

  public IIndexManager getIndexManager() {
    return this.indexManager;
  }

  public static void main( String[] args ) throws Exception
  {
    final int port = 0; // random port.

    Properties props = new Properties();
    props.setProperty(AbstractTripleStore.Options.QUADS, Boolean.toString(true));
    props.setProperty(AbstractTripleStore.Options.AXIOMS_CLASS, "com.bigdata.rdf.store.AbstractTripleStore.NoAxioms");
    //props.setProperty(AbstractTripleStore.Options.STATEMENT_IDENTIFIERS, Boolean.toString(false));
    //props.setProperty(AbstractTripleStore.Options.TRIPLES_MODE_WITH_PROVENANCE, Boolean.toString(true));
    props.setProperty(BigdataSail.Options.TRUTH_MAINTENANCE,Boolean.toString(false));

    final BigdataSail sail = new BigdataSail(props);
    final Repository repository = new BigdataSailRepository(sail);
    repository.initialize();

    try {

      final IIndexManager indexManager = sail.getIndexManager();

      final Map<String, String> initParams = new LinkedHashMap<String, String>();

      Thread t = new Thread(new App(port, indexManager, initParams));
      t.run();
    } finally {

      repository.shutDown();
      sail.shutDown();
      System.out.println("Halted.");
    }
  }
}
