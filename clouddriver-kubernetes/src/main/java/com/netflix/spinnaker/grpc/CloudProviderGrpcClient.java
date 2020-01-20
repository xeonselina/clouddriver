package com.netflix.spinnaker.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.coding.e.proto.CloudProviderProto;
import net.coding.e.proto.CloudProviderServiceGrpc;

public class CloudProviderGrpcClient {

  private static final int GRPC_MAX_MESSAGE_SIZE = 100 * 1024 * 1024;
  private static final Map<String, ManagedChannel> CHANNELS = new ConcurrentHashMap<>();
  private final CloudProviderServiceGrpc.CloudProviderServiceBlockingStub blockingStub;

  public CloudProviderGrpcClient(String host, int port) {
    blockingStub = CloudProviderServiceGrpc.newBlockingStub(openChannel(host, port));
  }

  public Channel openChannel(String host, int port) {
    return CHANNELS.computeIfAbsent(
        "coding",
        k ->
            NettyChannelBuilder.forAddress(host, port)
                .maxInboundMetadataSize(GRPC_MAX_MESSAGE_SIZE)
                .usePlaintext()
                .build());
  }

  public List<KubernetesGrpcAccount> getKubernetesAccounts() {
    try {
      CloudProviderProto.emptyRequest request =
          CloudProviderProto.emptyRequest.newBuilder().build();
      CloudProviderProto.KubernetesCPResponse response = blockingStub.getKubernetesCP(request);
      List<KubernetesGrpcAccount> kubernetesGrpcAccounts = new ArrayList<>();
      response.getKubernetesCPList().stream()
          .map(KubernetesGrpcAccount::fromProto)
          .forEach(kubernetesGrpcAccounts::add);
      return kubernetesGrpcAccounts;
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public List<TencentGrpcAccount> getTencentAccounts() {
    try {
      CloudProviderProto.emptyRequest request =
          CloudProviderProto.emptyRequest.newBuilder().build();
      CloudProviderProto.TencentCPResponse response = blockingStub.getTencentCP(request);
      List<TencentGrpcAccount> TencentGrpcAccounts = new ArrayList<>();
      response.getTencentCPList().stream()
          .map(TencentGrpcAccount::fromProto)
          .forEach(TencentGrpcAccounts::add);
      return TencentGrpcAccounts;
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }
}
