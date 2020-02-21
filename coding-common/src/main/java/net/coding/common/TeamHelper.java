package net.coding.common;

import org.apache.commons.lang3.StringUtils;

public class TeamHelper {

  public static final String TEAM = "team";

  public static String getSuffix(String teamId) {
    return StringUtils.join(TEAM, teamId);
  }

  public static String withSuffix(Integer teamId, String target) {
    return StringUtils.join(target, TEAM, teamId);
  }

  public static String replaceSuffix(String target) {
    return target.replaceFirst("(?i)team\\d+$", "");
  }
}
