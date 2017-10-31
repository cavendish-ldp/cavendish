package cavendish.ldp.digest;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cavendish.ldp.digest.impl.CRC32CSum;
import cavendish.ldp.digest.impl.ChecksumMessageDigest;
import cavendish.ldp.digest.impl.UnixSum;
import cavendish.ldp.headers.DigestSerializer;

public class DigestsForStream {

  private static final Logger LOG = LoggerFactory.getLogger(DigestsForStream.class);
  public static final String[] HTTP_MESSAGE_DIGESTS = new String[]{
      "ADLER32", "CRC32c",
      "MD5", "SHA",
      "SHA-256", "SHA-512",
      "UNIXsum", "UNIXcksum"
  };

  private static final byte[] GARBAGE_BUFFER = new byte[8192];

  public static Map<String,String> digestsForStream(InputStream input) throws IOException {
    Map<String, MessageDigest> digests = new HashMap<>(HTTP_MESSAGE_DIGESTS.length);
    digests.put("ADLER32", new ChecksumMessageDigest(new Adler32()));
    digests.put("CRC32c", new ChecksumMessageDigest(new CRC32CSum()));
    try {
      digests.put("MD5", MessageDigest.getInstance("MD5"));
    } catch (NoSuchAlgorithmException e) {
      LOG.warn("No message digest algorithm available for MD5");;
    }
    try {
      digests.put("SHA", MessageDigest.getInstance("SHA"));
    } catch (NoSuchAlgorithmException e) {
      LOG.warn("No message digest algorithm available for SHA");;
    }
    try {
      digests.put("SHA-256", MessageDigest.getInstance("SHA-256"));
    } catch (NoSuchAlgorithmException e) {
      LOG.warn("No message digest algorithm available for SHA-256");;
    }
    try {
      digests.put("SHA-512", MessageDigest.getInstance("SHA-512"));
    } catch (NoSuchAlgorithmException e) {
      LOG.warn("No message digest algorithm available for SHA-512");;
    }
    digests.put("UNIXsum", new ChecksumMessageDigest(new UnixSum()));
    digests.put("UNIXcksum", new ChecksumMessageDigest(new CRC32()));

    for(Entry<String, MessageDigest> entry:digests.entrySet()) {
      input = new DigestInputStream(input, entry.getValue());
    }
    for(int len = 0; len > -1; len = input.read(GARBAGE_BUFFER));
    input.close();
    Map<String, String> results = new HashMap<>(HTTP_MESSAGE_DIGESTS.length);
    for(Entry<String, MessageDigest> entry:digests.entrySet()) {
      results.put(entry.getKey(), DigestSerializer.serialize(entry.getValue()));
    }
    return results;
  }
}
