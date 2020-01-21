package com.netflix.spinnaker.grpc;

import java.util.List;
import lombok.Data;

@Data
public class TencentGrpcAccount {
  private String name;
  private String secretId;
  private String secretKey;
  private List<String> regions;

  public static TencentGrpcAccount fromProto(
      net.coding.e.proto.CloudProviderProto.TencentCP provider) {
    TencentGrpcAccount tencentGrpcAccount = new TencentGrpcAccount();
    tencentGrpcAccount.setName(provider.getName());
    tencentGrpcAccount.setSecretId(provider.getSecretId());
    tencentGrpcAccount.setSecretKey(provider.getSecretKey());
    tencentGrpcAccount.setRegions(provider.getRegionsList());
    return tencentGrpcAccount;
  }
}
