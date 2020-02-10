package com.netflix.spinnaker.grpc.client;

import com.netflix.spinnaker.grpc.bean.KubernetesGrpcAccount;
import com.netflix.spinnaker.grpc.bean.TencentGrpcAccount;
import com.netflix.spinnaker.grpc.converter.GrpcConverter;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.coding.cd.proto.CloudAccountProto;
import net.coding.cd.proto.GrpcCloudAccountServiceGrpc;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CloudAccountGrpcClient extends BaseClient {

  private GrpcCloudAccountServiceGrpc.GrpcCloudAccountServiceBlockingStub getStub() {
    return GrpcCloudAccountServiceGrpc.newBlockingStub(openChannel());
  }

  public List<KubernetesGrpcAccount> getKubernetesAccounts() {
    try {
      CloudAccountProto.emptyRequest request = CloudAccountProto.emptyRequest.newBuilder().build();
      CloudAccountProto.KubernetesResponse response = getStub().getKubernetes(request);
      return GrpcConverter.INSTANCE.proto2kubernetes(response.getKubernetesList());
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public List<TencentGrpcAccount> getTencentAccounts() {
    try {
      CloudAccountProto.emptyRequest request = CloudAccountProto.emptyRequest.newBuilder().build();
      CloudAccountProto.TencentResponse response = getStub().getTencent(request);
      return GrpcConverter.INSTANCE.proto2tencent(response.getTencentList());
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }
}
