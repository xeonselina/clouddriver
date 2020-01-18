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

  public List<KubernetesGrpcProto> doExecute() {
    try {
      CloudProviderProto.getCloudProviderRequest request =
          CloudProviderProto.getCloudProviderRequest.newBuilder().build();
      CloudProviderProto.CloudProviderResponse response = blockingStub.getCloudProvider(request);
      List<KubernetesGrpcProto> kubernetesGrpcProtos = new ArrayList<>();
      response.getCloudProviderList().stream()
          .map(KubernetesGrpcProto::fromProto)
          .forEach(kubernetesGrpcProtos::add);
      return kubernetesGrpcProtos;
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }
}
