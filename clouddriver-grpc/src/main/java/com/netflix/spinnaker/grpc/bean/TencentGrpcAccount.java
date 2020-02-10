package com.netflix.spinnaker.grpc.bean;

import com.netflix.spinnaker.fiat.model.resources.Permissions;
import java.util.List;
import lombok.Data;

@Data
public class TencentGrpcAccount {

  private String name;
  private String secretId;
  private String secretKey;
  private List<String> regions;
  private Permissions.Builder permissions;
}
