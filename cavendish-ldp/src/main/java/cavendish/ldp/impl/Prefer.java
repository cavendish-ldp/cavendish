package cavendish.ldp.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cavendish.ldp.api.SerializationPreference;

public class Prefer implements SerializationPreference {
  private final static Logger LOG = LoggerFactory.getLogger(Prefer.class);
  static final Pattern UNQUOTE = Pattern.compile("^\\\"(.*)\\\"$");

  private final String returnValue;
  private final Collection<String> include;
  private final Collection<String> omit;
  private boolean acknowledged = false;

  public Prefer(String returnValue) {
    this(returnValue, Collections.emptyList());
  }

  public Prefer(String returnValue, Iterable<String> include) {
    this(returnValue, Collections.emptyList(), Collections.emptyList());
  }

  public Prefer(String returnValue, Collection<String> include, Collection<String> omit) {
    this.returnValue = returnValue;
    this.include = include;
    this.omit = omit;
  }
  public boolean preferMinimal() {
    return this.returnValue.equals(MINIMAL);
  }
  private boolean includeRepresentation(String key, boolean defaultValue) {
    if (this.include.contains(key)) return true;
    if (this.omit.contains(key)) return false;
    return defaultValue;
  }
  private boolean omitRepresentation(String key, boolean defaultValue) {
    if (this.omit.contains(key)) return true;
    if (this.include.contains(key)) return false;
    return defaultValue;
  }
  public boolean includeContainment(boolean defaultValue) {
    return includeRepresentation(LDP_CONTAINMENT, defaultValue) && !preferMinimal();
  }
  public boolean omitContainment(boolean defaultValue) {
    return omitRepresentation(LDP_CONTAINMENT, defaultValue) && !preferMinimal();
  }
  public boolean includeMembership(boolean defaultValue) {
    return includeRepresentation(LDP_MEMBERSHIP, defaultValue);
  }
  public boolean omitMembership(boolean defaultValue) {
    return omitRepresentation(LDP_MEMBERSHIP, defaultValue);
  }
  public boolean includeMinimalContainer(boolean defaultValue) {
    return includeRepresentation(LDP_MINIMAL, defaultValue);
  }
  public String getReturn() {
    return this.returnValue;
  }
  public Iterable<String> getInclude() {
    return this.include;
  }
  public Iterable<String> getOmit() {
    return this.omit;
  }
  public void acknowledge(boolean value) {
    this.acknowledged = value;
  }
  public boolean wasAcknowledged() {
    return this.acknowledged;
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("return=").append(this.returnValue).append(';');
    for (String inc:this.include) {
      buf.append("include=").append(inc).append(';');
    }
    for (String omit:this.omit) {
      buf.append("omit=").append(omit).append(';');
    }
    buf.append(',');
    return buf.toString();
  }
  static String unquote(String value) {
    Matcher unquote = UNQUOTE.matcher(value);
    if (unquote.matches()) return unquote.group(1);
    return value;
  }
  public static Prefer parse(String value) {
    LOG.warn("Parsing Prefer: {}", value);
    String[] parts = value.split(";\\s*");
    String returnValue = null;
    Collection<String> omit = Collections.emptyList();
    Collection<String> include = Collections.emptyList();
    for (String part: parts) {
      String[] keyValue = part.split("\\s*=\\s*", 2);
      keyValue[1] = unquote(keyValue[1]);
      if (keyValue[0].equals("return")) {
        returnValue = keyValue[1];
      } else
      if (keyValue[0].equals("include")) {
        include = Arrays.asList(keyValue[1].split("\\s+"));
      } else
      if (keyValue[0].equals("omit")) {
        omit = Arrays.asList(keyValue[1].split("\\s+"));
      }
    }
    return new Prefer(returnValue, include, omit);
  }

  @Override
  public boolean includeTimeMap(boolean defaultValue) {
    return includeRepresentation(LDP_TIMEMAP, defaultValue);
  }

  @Override
  public void include(String include) {
    if (!this.include.contains(include)) this.include.add(include);
  }

  @Override
  public void omit(String omit) {
    if (!this.omit.contains(omit)) this.omit.add(omit);
  }
}
