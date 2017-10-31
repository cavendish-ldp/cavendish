package cavendish.ldp.digest.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class BitMasksTest {

  @Test
  public void testRoundTrip() {
    long input = 175285471;
    byte[] bytes = BitMasks.longToBytes(input);
    long output = BitMasks.unsignedBytesToLong(bytes);
    assertEquals(input, output);
  }

}
