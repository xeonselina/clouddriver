package com.netflix.spinnaker.grpc;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;

@Data
public class KubernetesGrpcAccount {
  private String name;
  private String context;
  private List<String> namespaces;
  private String kubeconfigContents;
  private Boolean serviceaccount;

  public static KubernetesGrpcAccount fromProto(
      net.coding.e.proto.CloudProviderProto.KubernetesCP provider) {
    KubernetesGrpcAccount kubernetesGrpcAccount = new KubernetesGrpcAccount();
    kubernetesGrpcAccount.setContext(provider.getContext());
    kubernetesGrpcAccount.setKubeconfigContents(provider.getKubeconfigContents());
    kubernetesGrpcAccount.setName(provider.getName());
    List<String> namespaces =
        Optional.ofNullable(provider.getNamespaces())
            .filter(e -> !Strings.isNullOrEmpty(e))
            .map(Collections::singletonList)
            .orElseGet(ArrayList::new);
    kubernetesGrpcAccount.setNamespaces(namespaces);
    kubernetesGrpcAccount.setServiceaccount(provider.getServiceaccount());
    return kubernetesGrpcAccount;
  }
}
