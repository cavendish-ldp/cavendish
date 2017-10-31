package cavendish.ldp.digest.impl;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Adapter class wrapping a java.util.zip.Checksum in a java.security.MessageDigest
 * @author barmintor
 *
 */
public class ChecksumMessageDigest extends MessageDigest implements BitMasks {

	private final Checksum checksum;

	public ChecksumMessageDigest(Checksum checksum, String algorithm) {
		super(algorithm);
		this.checksum = checksum;
	}

	/**
	 * Calculate RFC3230 ADLER32 digest value
	 * @param checksum
	 */
	public ChecksumMessageDigest(Adler32 checksum) {
		this(checksum, "ADLER32");
	}

	/**
	 * Calculate RFC3230 UNIXsum digest value
	 * @param checksum
	 */
	public ChecksumMessageDigest(UnixSum checksum) {
		this(checksum, "UNIXsum");
	}

	/**
	 * Calculate RFC3230 UNIXcksum digest value
	 * @param checksum
	 */
	public ChecksumMessageDigest(CRC32 checksum) {
		this(checksum, "UNIXcksum");
	}

	/**
	 * Calculate the CRC32c polynomial checksum
	 */
	public ChecksumMessageDigest(CRC32CSum checksum) {
		this(checksum, "CRC32c");
	}
	
	@Override
	public int engineGetDigestLength() {
		return Long.BYTES;
	}

	@Override
	protected void engineUpdate(byte input) {
		this.checksum.update(input);
	}

	@Override
	protected void engineUpdate(byte[] input, int offset, int len) {
		this.checksum.update(input, offset, len);
	}

	@Override
	protected byte[] engineDigest() {
		Long sum = this.checksum.getValue();
		engineReset();
		byte[] result = BitMasks.longToBytes(sum);
		return result;
	}

	@Override
	protected void engineReset() {
		this.checksum.reset();
	}

}
