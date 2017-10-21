package cavendish.ldp.impl;

import static org.junit.Assert.*;

import org.hamcrest.Matchers;
import org.junit.Test;

public class HeaderValueTest {

  private static final String ALL_PARAMS = "a=1;b=2;a=3;c=four";
  private static final String NO_PARAMS = "a string";
  private static final String WITH_PARAMS = NO_PARAMS + ";" + ALL_PARAMS;
  @Test
  public void testAllParams() {
    HeaderValue testObj = HeaderValue.parse(ALL_PARAMS);
    assertNull(testObj.value());
    Object[] actual = testObj.parameters().get("a").toArray();
    Object[] expected = new String[]{"1","3"};
    assertArrayEquals(expected, actual);
    actual = testObj.parameters().get("b").toArray();
    expected = new String[]{"2"};
    assertArrayEquals(expected, actual);
    actual = testObj.parameters().get("c").toArray();
    expected = new String[]{"four"};
    assertArrayEquals(expected, actual);
  }
  @Test
  public void testNoParams() {
    HeaderValue testObj = HeaderValue.parse(NO_PARAMS);
    assertEquals(NO_PARAMS, testObj.value());
    assertThat(testObj.parameters().entrySet(), Matchers.empty());
  }
  @Test
  public void testWithParams() {
    HeaderValue testObj = HeaderValue.parse(WITH_PARAMS);
    assertEquals(NO_PARAMS, testObj.value());
    Object[] actual = testObj.parameters().get("a").toArray();
    Object[] expected = new String[]{"1","3"};
    assertArrayEquals(expected, actual);
    actual = testObj.parameters().get("b").toArray();
    expected = new String[]{"2"};
    assertArrayEquals(expected, actual);
    actual = testObj.parameters().get("c").toArray();
    expected = new String[]{"four"};
    assertArrayEquals(expected, actual);
  }

}
