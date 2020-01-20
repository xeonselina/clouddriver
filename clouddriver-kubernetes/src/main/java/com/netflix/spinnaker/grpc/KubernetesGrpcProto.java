package com.netflix.spinnaker.grpc;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;

@Data
public class KubernetesGrpcProto {
  private String name;
  private String context;
  private List<String> namespaces;
  private String kubeconfigContents;
  private Boolean serviceaccount;

  public static KubernetesGrpcProto fromProto(
      net.coding.e.proto.CloudProviderProto.CloudProvider provider) {
    KubernetesGrpcProto kubernetesGrpcProto = new KubernetesGrpcProto();
    kubernetesGrpcProto.setContext(provider.getContext());
    kubernetesGrpcProto.setKubeconfigContents(provider.getKubeconfigContents());
    kubernetesGrpcProto.setName(provider.getName());
    List<String> namespaces =
        Optional.ofNullable(provider.getNamespaces())
            .filter(e -> !Strings.isNullOrEmpty(e))
            .map(Collections::singletonList)
            .orElseGet(ArrayList::new);
    kubernetesGrpcProto.setNamespaces(namespaces);
    kubernetesGrpcProto.setServiceaccount(provider.getServiceaccount());
    return kubernetesGrpcProto;
  }
}
