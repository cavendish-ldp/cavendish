package cavendish.ldp.headers;

import java.security.MessageDigest;
import java.util.Base64;

import cavendish.ldp.digest.impl.BitMasks;

/**
 * Serialize digest values according to RFC3230
 * output of ADLER32 (RFC1950) and CRC32c (RFC4960) are encoded as hexadecimal strings
 * output of UNIXsum and UNIXcksum are encoded as ASCII digit strings of first word of output
 * output MD5 and the SHA algorithms are base64 encoded
 * @see https://www.iana.org/assignments/http-dig-alg/http-dig-alg.txt
 * @author barmintor
 *
 */
public abstract class DigestSerializer implements BitMasks {
    public static String serialize(MessageDigest digest) {
    	return serialize(digest.digest(), digest.getAlgorithm());
    }

    /**
     * @param digest
     * @param digestAlgorithm
     * @return
     */
    public static String serialize(byte[] digest, String digestAlgorithm) {
    	String downcase = digestAlgorithm.toLowerCase();
    	if (downcase.equals("unixsum")) {
    		return "UNIXsum=" + Long.toString(BitMasks.unsignedBytesToLong(digest));
    	}
    	if (downcase.equals("unixcksum")) {
    		return "UNIXcksum=" + Long.toString(BitMasks.unsignedBytesToLong(digest));
    	}
    	if (downcase.equals("adler32")) {
    		return "ADLER32=" + String.format("%08x", BitMasks.unsignedBytesToLong(digest));
    	}
    	if (downcase.equals("crc32c")) {
    	  
    		return "CRC32c=" + String.format("%08x", BitMasks.unsignedBytesToLong(digest));
    	}
    	return digestAlgorithm.toUpperCase() + "=" + Base64.getEncoder().encodeToString(digest);
    }

}
