package cavendish.ldp.digest;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

public class DigestsForStreamTest {

  private final static byte[] INPUT = "dog".getBytes();

  @Test
  public void test() throws IOException {
    
    Map<String, String> actual = DigestsForStream.digestsForStream(new ByteArrayInputStream(INPUT));
    assertEquals(actual.get("UNIXsum"), "UNIXsum=314");
    assertEquals(actual.get("CRC32c"), "CRC32c=0a72a4df");
    assertEquals(actual.get("MD5"), "MD5=BtgOsMULSaUJtJ8kJOjIBQ==");
  }

}
