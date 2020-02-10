package com.netflix.spinnaker.clouddriver.tencent.config;

import com.netflix.spinnaker.fiat.model.resources.Permissions;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class TencentConfigurationProperties {
  private List<ManagedAccount> accounts = new ArrayList<ManagedAccount>();

  @Data
  public static class ManagedAccount {
    private String name;
    private String environment;
    private String accountType;
    private String project;
    private String secretId;
    private String secretKey;
    private List<String> regions;
    private Permissions.Builder permissions;
  }
}
