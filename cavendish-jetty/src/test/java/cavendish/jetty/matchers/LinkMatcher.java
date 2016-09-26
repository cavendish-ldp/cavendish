package cavendish.jetty.matchers;

import java.util.function.Consumer;

import com.jayway.restassured.response.Header;

public class LinkMatcher implements Consumer<Header> {

  final String rel;
  final String uri;

  private boolean matched = false;
  public LinkMatcher(String rel, String uri) {
    this.rel = rel;
    this.uri = uri;
  }

  @Override
  public void accept(Header h) {
    if (h == null) return;

    String t = h.getValue();
    boolean match = t.contains("rel=\"" + rel + "\"");
    match &= t.contains("<" + uri + ">");
    matched |= match;
  }

  public boolean isMatched() {
    return matched;
  }

  public void reset() {
    matched = false;
  }
}
