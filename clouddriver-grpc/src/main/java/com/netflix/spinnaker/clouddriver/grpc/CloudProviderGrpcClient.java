/*
 * Copyright 2020 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.grpc;

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

  public CloudProviderGrpcClient() {
    blockingStub = CloudProviderServiceGrpc.newBlockingStub(openChannel());
  }

  public Channel openChannel() {
    return CHANNELS.computeIfAbsent(
        "coding",
        k ->
            NettyChannelBuilder.forAddress("127.0.0.1", 20153)
                .maxInboundMetadataSize(GRPC_MAX_MESSAGE_SIZE)
                .usePlaintext()
                .build());
  }

  public List<CloudProviderProto.CloudProvider> doExecute() {
    try {
      CloudProviderProto.getCloudProviderRequest request =
          CloudProviderProto.getCloudProviderRequest.newBuilder().build();
      CloudProviderProto.CloudProviderResponse response = blockingStub.getCloudProvider(request);
      return response.getCloudProviderList();
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }
}
