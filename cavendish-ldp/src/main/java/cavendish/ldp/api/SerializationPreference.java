package cavendish.ldp.api;

public interface SerializationPreference {
  public boolean preferMinimal();
  public boolean includeContainment(boolean defaultValue);
  public boolean omitContainment(boolean defaultValue);
  public boolean includeMembership(boolean defaultValue);
  public boolean omitMembership(boolean defaultValue);
  public boolean includeMinimalContainer(boolean defaultValue);
  public void acknowledge(boolean value);
  public boolean wasAcknowledged();
}
