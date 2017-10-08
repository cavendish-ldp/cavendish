package cavendish.ldp.api;

import javax.ws.rs.core.HttpHeaders;

public interface LdpHeaders extends HttpHeaders {
  
  /**
   * See {@link <a href="https://tools.ietf.org/html/rfc5789#section-4.1">RFC 5789</a>}.
   */
  public static final String ACCEPT_PATCH = "Accept-Patch";

  /**
   * See {@link <a href="https://www.w3.org/TR/2015/REC-ldp-20150226/#header-accept-post">LDP 1.0 documentation</a>}.
   */
  public static final String ACCEPT_POST = "Accept-Post";

  /**
   * See {@link <a href="https://tools.ietf.org/html/rfc7240#section-2">RFC 7240</a>}.
   */
  public static final String PREFER = "Prefer";
  
  /**
   * See {@link <a href="https://tools.ietf.org/html/rfc7240#section-3">RFC 7240</a>}.
   */
  public static final String PREFERENCE_APPLIED = "Preference-Applied";
  
  /**
   * See {@link <a href="https://tools.ietf.org/html/rfc5023#section-9.7">RFC 5023</a>}.
   */
  public static final String SLUG = "Slug";
}
