package com.netflix.spinnaker.grpc;

import lombok.Data;

@Data
public class KubernetesGrpcProto {
  private String name;
  private String context;
  private String namespaces;
  private String kubeconfigContents;
  private Boolean serviceaccount;

  public static KubernetesGrpcProto fromProto(
      net.coding.e.proto.CloudProviderProto.CloudProvider provider) {
    KubernetesGrpcProto kubernetesGrpcProto = new KubernetesGrpcProto();
    kubernetesGrpcProto.setContext(provider.getContext());
    kubernetesGrpcProto.setKubeconfigContents(provider.getKubeconfigContents());
    kubernetesGrpcProto.setName(provider.getName());
    kubernetesGrpcProto.setNamespaces(provider.getNamespaces());
    kubernetesGrpcProto.setServiceaccount(provider.getServiceaccount());
    return kubernetesGrpcProto;
  }
}
