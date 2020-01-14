package com.netflix.spinnaker.clouddriver.tencent.deploy.description;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DestroyTencentServerGroupDescription extends AbstractTencentCredentialsDescription {
  private String serverGroupName;
  private String region;
  private String accountName;
}
