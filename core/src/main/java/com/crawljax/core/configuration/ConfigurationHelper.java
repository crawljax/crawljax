package com.crawljax.core.configuration;

import java.util.List;

/**
 * Helper class for configurations.
 *
 * @author Danny Roest (dannyroest@gmail.com)
 */
public final class ConfigurationHelper {

  private ConfigurationHelper() {
  }

  /**
   *Returns string representation of list. format: a, b, , c. Empty String allowed.
 @param items The items to be added to the string.
   * 
   */
  public static String listToStringEmptyStringAllowed(List<String> items) {
    StringBuilder str = new StringBuilder();
    int i = 0;
    for (String item : items) {
      if (i > 0) {
        str.append(", ");
      }
      str.append(item);
      i++;
    }
    return str.toString();
  }

  /**
   *Returns string representation of list. format: a, b, c.
 @param items The items to be added to the string.
   * 
   */
  public static String listToString(List<?> items) {
    StringBuilder str = new StringBuilder();
    for (Object item : items) {
      if (!str.toString().equals("")) {
        str.append(", ");
      }
      str.append(item.toString());
    }
    return str.toString();
  }

  /**
   *Returns int value of boolean, true=1 false=0.
 @param value The value to be converted
   * 
   */
  public static int booleanToInt(boolean value) {
    if (value) {
      return 1;
    } else {
      return 0;
    }
  }

}
