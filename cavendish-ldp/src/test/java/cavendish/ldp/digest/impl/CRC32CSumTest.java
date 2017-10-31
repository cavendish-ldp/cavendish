package cavendish.ldp.digest.impl;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class CRC32CSumTest {

	private static final byte[] INCREMENT = new byte[]{
			0x0, 0x1, 0x2, 0x3,
			0x4, 0x5, 0x6, 0x7,
			0x8, 0x9, 0xa, 0xb,
			0xc, 0xd, 0xe, 0xf,
			0x10, 0x11, 0x12, 0x13,
			0x14, 0x15, 0x16, 0x17,
			0x18, 0x19, 0x1a, 0x1b,
			0x1c, 0x1d, 0x1e, 0x1f
			};
	private static final byte[] DECREMENT = flip(INCREMENT);

	private static final byte[] TEST_SCSI = {

			0x01, (byte)0xC0, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00,
			0x14, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x04, 0x00,
			0x00, 0x00, 0x00, 0x14,
			0x00, 0x00, 0x00, 0x18,
			0x28, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00,
			0x02, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00
			};


	/**
	 * This expectation is given priority as it is relevant to the HTTP digest transport specs
	 * @see https://www.iana.org/assignments/http-dig-alg/http-dig-alg.xhtml#hash-alg
	 */
	@Test
	public void testDog() {
		String expected = "0a72a4df";
		byte[] input = "dog".getBytes();
		CRC32CSum test = new CRC32CSum();
		test.update(input,0,input.length);
		assertEquals(expected, String.format("%08x", test.getValue()));
	}

	/**
	 * examples from this document have byte-swapped expectations
     * @see https://tools.ietf.org/html/rfc3720#appendix-B.4
     */
	@Test
	public void testAllOff() {
		String expected = "aa36918a";
		byte[] input = new byte[32];
		Arrays.fill(input, (byte)0);
		CRC32CSum test = new CRC32CSum();
		test.update(input,0,input.length);
		assertEquals(expected, String.format("%08x", swap(test.getValue())));
	}

	/**
	 * examples from this document have byte-swapped expectations
     * @see https://tools.ietf.org/html/rfc3720#appendix-B.4
     */
	@Test
	public void testAllOn() {
		String expected = "43aba862";
		byte[] input = new byte[32];
		Arrays.fill(input, (byte)0xff);
		CRC32CSum test = new CRC32CSum();
		test.update(input,0,input.length);
		assertEquals(expected, String.format("%08x", swap(test.getValue())));
	}

	/**
	 * examples from this document have byte-swapped expectations
     * @see https://tools.ietf.org/html/rfc3720#appendix-B.4
     */
	@Test
	public void testIncrementing() {
		String expected = "4e79dd46";
		CRC32CSum test = new CRC32CSum();
		test.update(INCREMENT,0,INCREMENT.length);
		assertEquals(expected, String.format("%08x", swap(test.getValue())));
	}

	/**
	 * examples from this document have byte-swapped expectations
     * @see https://tools.ietf.org/html/rfc3720#appendix-B.4
     */
	@Test
	public void testDecrementing() {
		String expected = "5cdb3f11";
		CRC32CSum test = new CRC32CSum();
		test.update(DECREMENT,0,DECREMENT.length);
		assertEquals(expected, String.format("%08x", swap(test.getValue())));
	}

	/**
	 * examples from this document have byte-swapped expectations
     * @see https://tools.ietf.org/html/rfc3720#appendix-B.4
     */
	@Test
	public void testScsiReadVector() {
		String expected = "563a96d9";
		CRC32CSum test = new CRC32CSum();
		test.update(TEST_SCSI,0,TEST_SCSI.length);
		assertEquals(expected, String.format("%08x", swap(test.getValue())));
	}

	/**
	 * @see http://reveng.sourceforge.net/crc-catalogue/17plus.htm#crc.cat.crc-32c
	 */
	@Test
	public void testDigits() {
		String expected = "e3069283";
		byte[] input = "123456789".getBytes();
		CRC32CSum test = new CRC32CSum();
		test.update(input,0,input.length);
		assertEquals(expected, String.format("%08x", test.getValue()));
	}

	private static long swap(long in) {
		return (in&0xff)<<24 | (in&0xff00)<<8 | (in&0xff0000)>>8 | (in>>24)&0xff;
	}

	private static byte[] flip(byte[] in) {
		byte[] out = new byte[in.length];
		for (int i=1;i<=in.length;i++) out[i - 1] = in[in.length - i];
		return out;
	}
}
