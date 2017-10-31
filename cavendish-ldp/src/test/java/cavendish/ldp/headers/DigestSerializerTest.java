package cavendish.ldp.headers;

import static org.junit.Assert.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import cavendish.ldp.digest.impl.CRC32CSum;
import cavendish.ldp.digest.impl.ChecksumMessageDigest;
import cavendish.ldp.digest.impl.UnixSum;

public class DigestSerializerTest {

	private final static byte[] INPUT = "dog".getBytes();

	@Test
	public void testAsciiDigits() {
		String expected = "UNIXsum=314";
		MessageDigest digester = new ChecksumMessageDigest(new UnixSum());
		digester.update(INPUT);
		String actual = DigestSerializer.serialize(digester);
		assertEquals(expected, actual);
	}

	@Test
	public void testHex() {
		String expected = "CRC32c=0a72a4df";
		MessageDigest digester = new ChecksumMessageDigest(new CRC32CSum());
		digester.update(INPUT);
		String actual = DigestSerializer.serialize(digester);
		assertEquals(expected, actual);
	}

	@Test
	public void testBase64() throws NoSuchAlgorithmException {
		String expected = "MD5=BtgOsMULSaUJtJ8kJOjIBQ==";
		MessageDigest digester = MessageDigest.getInstance("MD5");
		digester.update(INPUT);
		String actual = DigestSerializer.serialize(digester);
		assertEquals(expected, actual);
	}
	
}
