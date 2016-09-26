package cavendish.jetty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;

import com.bigdata.journal.IIndexManager;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.sail.webapp.NanoSparqlServer;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.util.config.NicUtil;

import cavendish.jetty.handler.LdpHandler;

public class App implements Runnable
{
  private static final Logger log = Logger
      .getLogger(App.class);

  private int port;
  private final IIndexManager indexManager;
  private final Properties initParams;
  private final Server server;
  // if sail or repo are cleaned up in GC, they will shutdown, so must be members
  private final Repository repository;
  private static Properties loadProperties(String configPath) {
    Properties props = new Properties();
    try {
      props.load(new FileInputStream(configPath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return props;
  }

  public App(int port, String configPath) throws RepositoryException {
    this(port, loadProperties(configPath));
  }

  public App(int port, Properties initParams) throws RepositoryException {
    this.port = port;
    this.initParams = initParams;
    final BigdataSail sail = new BigdataSail(initParams);
    repository = new BigdataSailRepository(sail);
    repository.initialize();


    indexManager = sail.getIndexManager();

    LdpHandler handler = new LdpHandler(new String[]{""});
    handler.setIndexManager(indexManager);
    server = new Server(port);
    HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[] { handler, new DefaultHandler() });
    server.setHandler(handler);
    server.setStopTimeout(100);
  }

  public int port()
  {
    return this.port;
  }

  public void run() {
    start();
    try {
      join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  public void start()
  {
    try {
      server.start();
      NanoSparqlServer.awaitServerStart(server);
      port = ((ServerConnector)server.getConnectors()[0]).getLocalPort();

      boolean loopbackOk = true;
      String hostAddr = NicUtil.getIpAddress("default.nic",
          "default", loopbackOk);

      if (hostAddr == null) {

        hostAddr = "localhost";

      }

      final String serviceURL = new URL("http", hostAddr, port, ""/* file */)
          .toExternalForm();

      System.out.println("serviceURL: " + serviceURL);
    } catch (Throwable t) {
      stop();
      t.printStackTrace();
      log.error(t, t);
    }

  }

  public void join() throws InterruptedException {
    server.join();
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

  public Server getServer() {
    return this.server;
  }

  public static void main( String[] args ) throws Exception
  {
    final int port = Integer.parseInt(args[0]); // random port.

    App app = null;
    String configPath = (args.length > 1) ? args[2] : null;
    if (configPath != null) {
      app = new App(port, configPath);
    } else {
      Properties props = new Properties();
      props.setProperty(AbstractTripleStore.Options.QUADS, Boolean.toString(true));
      props.setProperty(AbstractTripleStore.Options.AXIOMS_CLASS, "com.bigdata.rdf.store.AbstractTripleStore.NoAxioms");
      //props.setProperty(AbstractTripleStore.Options.STATEMENT_IDENTIFIERS, Boolean.toString(false));
      //props.setProperty(AbstractTripleStore.Options.TRIPLES_MODE_WITH_PROVENANCE, Boolean.toString(true));
      props.setProperty(BigdataSail.Options.TRUTH_MAINTENANCE,Boolean.toString(false));
      app = new App(port, props);
    }
    try {

      app.start();
      app.join();
    } finally {
      app.stop();
      System.out.println("Halted.");
    }
  }
}
