package cavendish.jetty;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.openrdf.sail.SailException;

import com.bigdata.journal.IBufferStrategy;
import com.bigdata.journal.Journal;
import com.bigdata.journal.RWStrategy;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rwstore.sector.MemStrategy;

public abstract class BaseTestCase {
    private static ExecutorService pool = Executors.newCachedThreadPool();
    static int port = 8888;
    static App app = null;
    static Thread server = null;
    static BigdataSail sail = null;
    @BeforeClass
    public static void beforeAll() throws Exception {
        final Properties props = new Properties();
        new File("test.jnl").delete();
        props.put("com.bigdata.journal.AbstractJournal.bufferMode","DiskRW"); // DiskRW,MemStore
        props.put("com.bigdata.journal.AbstractJournal.file","test.jnl");
        props.put("com.bigdata.journal.AbstractJournal.readOnly","false");
        //sail = new BigdataSail(props);
        //final Repository repository = new BigdataSailRepository(sail);
        //repository.initialize();

        /**
        BigdataSailConnection c = sail.getConnection();
        try {
          org.junit.Assert.assertTrue("Connections are writeable", !c.isReadOnly());
        } finally {
          c.close();
        }
        **/
        try {

            final Journal indexManager = new Journal(props);
            IBufferStrategy strat = indexManager.getBufferStrategy();
            org.junit.Assert.assertTrue(strat instanceof RWStrategy);

            final Map<String, String> initParams = new LinkedHashMap<String, String>();
            //initParams.put("com.bigdata.rdf.sail.webapp.ConfigParams.propertyFile", "RWStore.properties");
            //System.setProperty("com.bigdata.rdf.sail.webapp.ConfigParams.propertyFile", "RWStore.properties");
            app = new App(port, indexManager, initParams);
            server = new Thread(app);
            pool.submit(server);
        } finally {

        }
    }

    protected URL getBaseUrl() {
      try {
        return new URL("http://localhost:" + Integer.toString(app.port()) + "/ldp");
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

    @AfterClass
    public static void afterAll() throws SailException {
	    if (server != null) server.interrupt();
	    if (sail != null) sail.shutDown();
	    pool.shutdown();
    }
}
