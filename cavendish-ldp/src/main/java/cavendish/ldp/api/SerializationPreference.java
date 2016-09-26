package cavendish.ldp.api;

public interface SerializationPreference {
  public String MINIMAL = "minimal";
  public String REPRESENTATION = "representation";
  public String LDP_MINIMAL = "http://www.w3.org/ns/ldp#PreferMinimalContainer";
  public String LDP_CONTAINMENT = "http://www.w3.org/ns/ldp#PreferContainment";
  public String LDP_EMPTY = "http://www.w3.org/ns/ldp#PreferEmptyContainer";
  public String LDP_MEMBERSHIP = "http://www.w3.org/ns/ldp#PreferMembership";
  public String LDP_TIMEMAP = "http://www.w3.org/ns/ldp#PreferTimeMap";

  public boolean preferMinimal();
  public boolean includeContainment(boolean defaultValue);
  public boolean omitContainment(boolean defaultValue);
  public boolean includeMembership(boolean defaultValue);
  public boolean omitMembership(boolean defaultValue);
  public boolean includeMinimalContainer(boolean defaultValue);
  public boolean includeTimeMap(boolean defaultValue);
  public void acknowledge(boolean value);
  public boolean wasAcknowledged();
  public void include(String include);
  public void omit(String omit);
}
