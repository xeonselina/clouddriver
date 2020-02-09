package com.netflix.spinnaker.grpc.client;

import com.netflix.spinnaker.grpc.GrpcSettings;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BaseClient {

  @Resource
  private GrpcSettings grpcSettings;
  private static final int GRPC_MAX_MESSAGE_SIZE = 100 * 1024 * 1024;
  private static final Map<String, ManagedChannel> CHANNELS = new ConcurrentHashMap<>();

  public Channel openChannel() {
    return CHANNELS.computeIfAbsent(this.getClass().getSimpleName(), k -> NettyChannelBuilder
      .forAddress(grpcSettings.getHost(), grpcSettings.getPort())
      .maxInboundMetadataSize(GRPC_MAX_MESSAGE_SIZE)
      .usePlaintext()
      .build());
  }

}
