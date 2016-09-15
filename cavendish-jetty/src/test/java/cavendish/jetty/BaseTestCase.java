package cavendish.jetty;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.After;
import org.junit.AfterClass;
import org.openrdf.repository.Repository;
import org.openrdf.sail.SailException;

import com.bigdata.journal.BufferMode;
import com.bigdata.journal.IIndexManager;
import com.bigdata.journal.Journal;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;

import cavendish.jetty.handler.LdpHandler;

public abstract class BaseTestCase {
    //private static ExecutorService pool = Executors.newCachedThreadPool();
    static int port = 8888;
    Server server = null;
    // if sail or repo are cleaned up in GC, they will shutdown, so must be members
    BigdataSail sail = null;
    Repository repository = null;
    File journalPath = null;
    @Before
    public void beforeAll() throws Exception {
        final Properties props = new Properties();
        props.load(new FileInputStream("src/test/webapp/WEB-INF/RWStore.properties"));
        //props.remove("com.bigdata.journal.AbstractJournal.file");
        journalPath = new File(props.getProperty("com.bigdata.journal.AbstractJournal.file")).getAbsoluteFile();
        journalPath.delete();
        journalPath.createNewFile();
        //props.setProperty("com.bigdata.journal.AbstractJournal.bufferMode", BufferMode.MemStore.toString());
        try {

          sail = new BigdataSail(props);
          repository = new BigdataSailRepository(sail);
          repository.initialize();

          final IIndexManager indexManager = sail.getIndexManager();

          final LdpHandler handler = new LdpHandler(new String[]{""});
          handler.setIndexManager(indexManager);

          HandlerList handlers = new HandlerList();
          handlers.setHandlers(new Handler[] { handler, new DefaultHandler() });

          this.server = new Server(port);
          this.server.setHandler(handler);
          this.server.setStopTimeout(100);
          server.start();
          assertTrue(((Journal)indexManager).getBufferStrategy().isOpen());
          assertTrue(journalPath.exists());
        } finally {

        }
    }

    protected URL getBaseUrl() {
      try {
        return new URL("http://localhost:" + Integer.toString(port) + "/");
      } catch (MalformedURLException e) {
        throw new RuntimeException("HTTP protocol not supperted?", e);
      }
    }

    protected File getTargetWorkingDir() {
      String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
      File targetDir = new File(relPath, "ldp-testsuite");
      if(!targetDir.exists()) {
          Assume.assumeTrue("Could not create report-directory", targetDir.mkdir());
      }
      return targetDir;
    }

    @After
    public void afterAll() throws Exception {
	    if (server != null)  server.stop();
	    if (sail != null) sail.shutDown();
      journalPath.delete();
   }
}
