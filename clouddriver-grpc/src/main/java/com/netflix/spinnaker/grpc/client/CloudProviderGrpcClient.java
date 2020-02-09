package com.netflix.spinnaker.grpc.client;

import com.netflix.spinnaker.grpc.bean.KubernetesGrpcAccount;
import com.netflix.spinnaker.grpc.bean.TencentGrpcAccount;
import com.netflix.spinnaker.grpc.converter.GrpcConverter;

import net.coding.e.proto.CloudProviderProto;
import net.coding.e.proto.CloudProviderServiceGrpc;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import io.grpc.StatusRuntimeException;

@Component
public class CloudProviderGrpcClient  extends BaseClient {

  private CloudProviderServiceGrpc.CloudProviderServiceBlockingStub getStub() {
    return CloudProviderServiceGrpc.newBlockingStub(openChannel());
  }

  public List<KubernetesGrpcAccount> getKubernetesAccounts() {
    try {
      CloudProviderProto.emptyRequest request =
          CloudProviderProto.emptyRequest.newBuilder().build();
      CloudProviderProto.KubernetesCPResponse response =  getStub().getKubernetesCP(request);
      return GrpcConverter.INSTANCE.proto2kubernetes(response.getKubernetesCPList());
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public List<TencentGrpcAccount> getTencentAccounts() {
    try {
      CloudProviderProto.emptyRequest request =
          CloudProviderProto.emptyRequest.newBuilder().build();
      CloudProviderProto.TencentCPResponse response = getStub().getTencentCP(request);
      return GrpcConverter.INSTANCE.proto2tencent(response.getTencentCPList());
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }
}
