package com.netflix.spinnaker.grpc.bean;

import java.util.List;

import lombok.Data;

@Data
public class KubernetesGrpcAccount {

  private String name;
  private String context;
  private List<String> namespaces;
  private String kubeconfigContents;
  private Boolean serviceaccount;

}
