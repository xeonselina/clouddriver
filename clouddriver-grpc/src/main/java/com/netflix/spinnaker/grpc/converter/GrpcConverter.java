package com.netflix.spinnaker.grpc.converter;

import com.google.common.base.Strings;
import com.google.protobuf.ProtocolStringList;
import com.netflix.spinnaker.fiat.model.Authorization;
import com.netflix.spinnaker.fiat.model.resources.Permissions;
import com.netflix.spinnaker.grpc.bean.KubernetesGrpcAccount;
import com.netflix.spinnaker.grpc.bean.TencentGrpcAccount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.coding.cd.proto.CloudAccountProto;
import net.coding.common.TeamHelper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GrpcConverter {
  GrpcConverter INSTANCE = Mappers.getMapper(GrpcConverter.class);

  default TencentGrpcAccount proto2tencent(CloudAccountProto.Tencent provider) {
    if (provider == null) {
      return null;
    }

    TencentGrpcAccount tencentGrpcAccount = new TencentGrpcAccount();
    ProtocolStringList protocolStringList = provider.getRegionsList();
    if (protocolStringList != null) {
      tencentGrpcAccount.setRegions(new ArrayList<>(protocolStringList));
    }
    tencentGrpcAccount.setName(TeamHelper.withSuffix(provider.getTeamId(), provider.getName()));
    tencentGrpcAccount.setSecretId(provider.getSecretId());
    tencentGrpcAccount.setSecretKey(provider.getSecretKey());
    tencentGrpcAccount.setPermissions(
        getPermissionsBuilder(provider.getTeamId(), provider.getRolesList()));
    return tencentGrpcAccount;
  }

  default KubernetesGrpcAccount proto2kubernetes(CloudAccountProto.Kubernetes provider) {
    if (provider == null) {
      return null;
    }
    KubernetesGrpcAccount kubernetesGrpcAccount = new KubernetesGrpcAccount();
    kubernetesGrpcAccount.setContext(provider.getContext());
    kubernetesGrpcAccount.setKubeconfigContents(provider.getKubeconfigContents());
    kubernetesGrpcAccount.setName(TeamHelper.withSuffix(provider.getTeamId(), provider.getName()));
    List<String> namespaces =
        Optional.ofNullable(provider.getNamespaces())
            .filter(e -> !Strings.isNullOrEmpty(e))
            .map(Collections::singletonList)
            .orElseGet(ArrayList::new);
    kubernetesGrpcAccount.setNamespaces(namespaces);
    kubernetesGrpcAccount.setServiceaccount(provider.getServiceaccount());
    kubernetesGrpcAccount.setPermissions(
        getPermissionsBuilder(provider.getTeamId(), provider.getRolesList()));
    return kubernetesGrpcAccount;
  }

  List<TencentGrpcAccount> proto2tencent(List<CloudAccountProto.Tencent> providers);

  List<KubernetesGrpcAccount> proto2kubernetes(List<CloudAccountProto.Kubernetes> providers);

  default Permissions.Builder getPermissionsBuilder(Integer teamId, List<String> roles) {
    if (roles == null || roles.isEmpty()) {
      return null;
    }
    Permissions.Builder permissions = new Permissions.Builder();
    roles.forEach(
        role -> {
          permissions.add(Authorization.READ, TeamHelper.withSuffix(teamId, role));
          permissions.add(Authorization.WRITE, TeamHelper.withSuffix(teamId, role));
          permissions.add(Authorization.EXECUTE, TeamHelper.withSuffix(teamId, role));
        });
    return permissions;
  }
}
