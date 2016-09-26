package cavendish.ldp.impl;

import cavendish.ldp.api.SerializationPreference;

public class DefaultSerializationPreference implements SerializationPreference {

  @Override
  public boolean preferMinimal() {
    return false;
  }

  @Override
  public boolean includeContainment(boolean defaultValue) {
    return defaultValue;
  }

  @Override
  public boolean includeMembership(boolean defaultValue) {
    return defaultValue;
  }

  @Override
  public boolean includeMinimalContainer(boolean defaultValue) {
    return defaultValue;
  }

  @Override
  public void acknowledge(boolean value) {      
  }

  @Override
  public boolean wasAcknowledged() {
    return false;
  }

  @Override
  public boolean omitContainment(boolean defaultValue) {
    return defaultValue;
  }

  @Override
  public boolean omitMembership(boolean defaultValue) {
    return defaultValue;
  }

  @Override
  public boolean includeTimeMap(boolean defaultValue) {
    return defaultValue;
  }

  @Override
  public void include(String include) {
    
  }

  @Override
  public void omit(String omit) {
    
  }

  public static class TimeMap extends DefaultSerializationPreference {
    @Override
    public boolean includeTimeMap(boolean defaultValue) {
      return true;
    }
  }
}