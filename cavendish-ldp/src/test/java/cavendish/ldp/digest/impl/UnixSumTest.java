package cavendish.ldp.digest.impl;

import static org.junit.Assert.*;


import org.junit.Test;

public class UnixSumTest {

	@Test
	public void testDigits() {
		UnixSum test = new UnixSum();
		byte [] input = "123456789".getBytes();
		test.update(input, 0, input.length);
		assertEquals(477, test.getValue());
	}

	@Test
	public void testDog() {
		UnixSum test = new UnixSum();
		byte [] input = "dog".getBytes();
		test.update(input, 0, input.length);
		assertEquals(314, test.getValue());
	}
}
