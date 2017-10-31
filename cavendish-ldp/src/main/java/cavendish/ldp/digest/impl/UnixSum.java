package cavendish.ldp.digest.impl;

import java.util.zip.Checksum;

/**
 * Implementing historic UNIXsum digest to support RFC3230
 * @see https://tools.ietf.org/html/rfc3230
 * @see http://pubs.opengroup.org/onlinepubs/7908799/xcu/sum.html
 * @author barmintor
 *
 */
public class UnixSum implements Checksum, BitMasks {

	private long s = 0;
	@Override
	public void update(int b) {
		this.s = (this.s + b) & BITMASK_32;
	}

	@Override
	public void update(byte[] buf, int off, int len) {
		long s = 0;
		for (int i = 0; i < len; i++) {
			s += buf[off + i];
		}
		this.s = (this.s + s) & BITMASK_32;
	}

	@Override
	public long getValue() {
		long r = (s & BITMASK_16) + ((s & BITMASK_32) >> 16);
		return (r & BITMASK_16) + (r >> 16);
	}

	@Override
	public void reset() {
		this.s = 0;
	}

}
