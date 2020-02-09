package com.netflix.spinnaker.grpc;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class GrpcSettings {

  @Value("${cd.coding.grpc.host:localhost}")
  private String host;

  @Value("${cd.coding.grpc.port:28888}")
  private int port;
}
