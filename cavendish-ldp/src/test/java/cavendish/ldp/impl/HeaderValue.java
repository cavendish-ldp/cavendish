package cavendish.ldp.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class HeaderValue {
  private static final Pattern SPECIAL = Pattern.compile("[()<>@,;:\\\".\\[\\]]\\s");

  private String value;
  private Map<String, List<String>> parameters;

  private HeaderValue(String value, Map<String, List<String>> params) {
      this.value = value;
      this.parameters = params;
  }

  public Map<String, List<String>> parameters() {
    return this.parameters;
  }

  public String value() {
    return this.value;
  }

  public String toString() {
    StringBuilder result = new StringBuilder();
    if (value != null) {
      if (SPECIAL.matcher(value).matches()) {
        result.append('"').append(value).append('"');
      } else result.append(value);
    }
    if (parameters != null) {
      for (Entry<String, List<String>> entry: parameters.entrySet()) {
        List<String> pValues = entry.getValue();
        for (String pValue: pValues) {
          result.append(';').append(entry.getKey()).append('=');
          if (SPECIAL.matcher(pValue).matches()) {
            result.append('"').append(pValue).append('"');
          } else result.append(pValue);
        }
      }
    }
    if (result.charAt(0) == ';') result.deleteCharAt(0);
    return result.length() == 0 ? null : result.toString();
  }

  public static HeaderValue parse(String headerValue) {
    String [] components = headerValue.split(";");
    String value = null;
    Map<String,List<String>> params = new HashMap<>();
    for (String component: components) {
      String [] parts = component.split("=",2);
      if (parts.length == 1) {
        value = parts[0];
      } else {
        List<String> t = params.computeIfAbsent(parts[0], (k) -> { return new ArrayList<String>(); });
        t.add(parts[1]);
      }
    }
    return new HeaderValue(value, params);
  }
  public static HeaderValue[] parseValues(String headerText) {
    String[] headerValues = headerText.split(",");
    HeaderValue[] result = new HeaderValue[headerValues.length];
    for (int i=0;i<headerValues.length;i++){
      result[i] = parse(headerValues[i]);
    }
    return result;
  }
}
