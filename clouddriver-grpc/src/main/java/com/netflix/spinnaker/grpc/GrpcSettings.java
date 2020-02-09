package com.netflix.spinnaker.grpc;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class GrpcSettings {

  @Value("${cd.coding.grpc.host:9.134.32.36}")
  private String host;

  @Value("${cd.coding.grpc.port:20153}")
  private int port;
}
