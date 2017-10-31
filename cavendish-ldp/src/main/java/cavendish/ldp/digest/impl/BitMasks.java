package cavendish.ldp.digest.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public interface BitMasks {
  long BITMASK_8 = 0xffL;
  long BITMASK_16 = 0xffffL;
  long BITMASK_32 = 0xffffffffL;

  public static byte[] longToBytes(long l) {
    byte[] result = new byte[Long.BYTES];
    Arrays.fill(result,  (byte)0x00);
    for (int i=1; i <= Long.BYTES; i++) {
      result[Long.BYTES - i] = (byte)(l & BITMASK_8);
      l = l >> 8;
    }
    return result;
  }

  public static long unsignedBytesToLong(byte[] bytes) {
    if (bytes.length > Long.BYTES) {
      throw new IllegalArgumentException("Too many digest bytes for long: " + Integer.toString(bytes.length));
    }
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    bb.order(ByteOrder.BIG_ENDIAN);
    return bb.getLong() & 0xffffffffl;
  }
}
