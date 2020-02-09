package com.netflix.spinnaker.grpc.converter;

import com.google.common.base.Strings;

import com.netflix.spinnaker.grpc.bean.KubernetesGrpcAccount;
import com.netflix.spinnaker.grpc.bean.TencentGrpcAccount;

import net.coding.e.proto.CloudProviderProto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mapper
public interface GrpcConverter {
  GrpcConverter INSTANCE = Mappers.getMapper(GrpcConverter.class);

  @Mapping(target = "regions",source = "regionsList")
  TencentGrpcAccount proto2tencent(CloudProviderProto.TencentCP provider);

  default KubernetesGrpcAccount proto2kubernetes(CloudProviderProto.KubernetesCP provider) {
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

  List<TencentGrpcAccount> proto2tencent(List<CloudProviderProto.TencentCP> providers);

  List<KubernetesGrpcAccount> proto2kubernetes(List<CloudProviderProto.KubernetesCP> providers);

}
