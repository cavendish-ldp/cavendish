package cavendish.jetty.headers;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

public class PreferTest {
  @Test
  public void testReturn() {
    String expected = "returnParamValue";
    String input = "return=" + expected;
    Prefer representation = Prefer.parse(input);
    assertEquals("Failed to parse simple return param", expected, representation.getReturn());
    input = "return=\"" + expected + "\"";
    representation = Prefer.parse(input);
    assertEquals("Failed to parse quoted return param", expected, representation.getReturn());
  }
  @Test
  public void testInclude() {
    String expected = "includeParamValue";
    String input = "return=representation; include=" + expected;
    Prefer representation = Prefer.parse(input);
    Iterator<String> actual = representation.getInclude().iterator();
    assertTrue("Did not parse expected include param", actual.hasNext());
    assertEquals("Failed to parse simple return param", expected, actual.next());
    assertFalse("Unexpected include params", actual.hasNext());
  }
  @Test
  public void testHeaderExample() {
    String input = "return=representation; include=\"http://www.w3.org/ns/ldp#PreferMinimalContainer\"";
    Prefer representation = Prefer.parse(input);
    assertFalse(representation.preferMinimal());
    assertTrue("Did not parse include param", representation.includeMinimalContainer(false));
    input = "return=minimal";
    representation = Prefer.parse(input);
    assertTrue(representation.preferMinimal());
    assertFalse("Did not parse include param", representation.includeMinimalContainer(false));
    input = "return=representation; include=\"http://www.w3.org/ns/ldp#PreferContainment\"";
    representation = Prefer.parse(input);
    assertFalse(representation.preferMinimal());
    assertTrue("Did not parse include param", representation.includeContainment(false));
    input = "return=minimal; include=\"http://www.w3.org/ns/ldp#PreferContainment\"";
    representation = Prefer.parse(input);
    assertTrue(representation.preferMinimal());
    assertFalse("Did not parse return param", representation.includeContainment(false));
  }
}
