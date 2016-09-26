package cavendish.jetty;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.junit.Assume;
import org.junit.Before;
import org.junit.After;

import com.bigdata.journal.Journal;

public abstract class BaseTestCase {
    //private static ExecutorService pool = Executors.newCachedThreadPool();
    static int port = 8888;
    static String TEST_PROPERTIES = "src/test/webapp/WEB-INF/RWStore.properties";
    File journalPath = null;
    App app = null;
    @Before
    public void beforeAll() throws Exception {
        final Properties props = new Properties();
        props.load(new FileInputStream(TEST_PROPERTIES));
        //props.remove("com.bigdata.journal.AbstractJournal.file");
        journalPath = new File(props.getProperty("com.bigdata.journal.AbstractJournal.file")).getAbsoluteFile();
        journalPath.delete();
        journalPath.createNewFile();
        app = new App(port, TEST_PROPERTIES);
        app.start();
        //props.setProperty("com.bigdata.journal.AbstractJournal.bufferMode", BufferMode.MemStore.toString());
        try {
        /**

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
          **/
          assertTrue(((Journal)app.getIndexManager()).getBufferStrategy().isOpen());
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
	    // if (server != null)  server.stop();
	    // if (sail != null) sail.shutDown();
      app.stop();
      journalPath.delete();
   }
}
