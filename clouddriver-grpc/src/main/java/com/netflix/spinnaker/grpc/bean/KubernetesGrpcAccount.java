package com.netflix.spinnaker.grpc.bean;

import com.netflix.spinnaker.fiat.model.resources.Permissions;
import java.util.List;
import lombok.Data;

@Data
public class KubernetesGrpcAccount {

  private String name;
  private String context;
  private List<String> namespaces;
  private String kubeconfigContents;
  private Boolean serviceaccount;
  private Permissions.Builder permissions;
}
