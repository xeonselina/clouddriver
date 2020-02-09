package com.netflix.spinnaker.clouddriver.tencent.provider.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spectator.api.Registry;
import com.netflix.spinnaker.cats.agent.Agent;
import com.netflix.spinnaker.clouddriver.tencent.provider.TencentInfrastructureProvider;
import com.netflix.spinnaker.clouddriver.tencent.provider.agent.TencentImageCachingAgent;
import com.netflix.spinnaker.clouddriver.tencent.provider.agent.TencentInstanceCachingAgent;
import com.netflix.spinnaker.clouddriver.tencent.provider.agent.TencentInstanceTypeCachingAgent;
import com.netflix.spinnaker.clouddriver.tencent.provider.agent.TencentKeyPairCachingAgent;
import com.netflix.spinnaker.clouddriver.tencent.provider.agent.TencentLoadBalancerCachingAgent;
import com.netflix.spinnaker.clouddriver.tencent.provider.agent.TencentLoadBalancerInstanceStateCachingAgent;
import com.netflix.spinnaker.clouddriver.tencent.provider.agent.TencentNetworkCachingAgent;
import com.netflix.spinnaker.clouddriver.tencent.provider.agent.TencentSecurityGroupCachingAgent;
import com.netflix.spinnaker.clouddriver.tencent.provider.agent.TencentServerGroupCachingAgent;
import com.netflix.spinnaker.clouddriver.tencent.provider.agent.TencentSubnetCachingAgent;
import com.netflix.spinnaker.clouddriver.tencent.security.TencentNamedAccountCredentials;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Import(com.netflix.spinnaker.config.TencentConfiguration.class)
@EnableConfigurationProperties
@Slf4j
public class TencentInfrastructureProviderConfig {

  private final ObjectMapper objectMapper;
  private final Registry registry;

  public TencentInfrastructureProviderConfig(ObjectMapper objectMapper,
                                             Registry registry) {
    this.objectMapper = objectMapper;
    this.registry = registry;
  }

  @Bean
  public TencentInfrastructureProvider tencentInfrastructureProvider() {
    return new TencentInfrastructureProvider(new ArrayList<>());
  }

  public Collection<Agent> syncAgents(TencentNamedAccountCredentials credential) {
    Collection<Agent> agents = new ArrayList<>();
    credential
      .getRegions()
      .forEach(
        region -> {
          agents.add(
            new TencentServerGroupCachingAgent(
              credential,
              objectMapper,
              registry,
              (region).getName()));

          agents.add(
            new TencentInstanceTypeCachingAgent(
              credential,
              objectMapper,
              (region).getName()));

          agents.add(
            new TencentKeyPairCachingAgent(
              credential,
              objectMapper,
              (region).getName()));

          agents.add(
            new TencentImageCachingAgent(
              credential,
              objectMapper,
              (region).getName()));

          agents.add(
            new TencentInstanceCachingAgent(
              credential,
              objectMapper,
              (region).getName()));

          agents.add(
            new TencentLoadBalancerCachingAgent(
              credential,
              objectMapper,
              registry,
              (region).getName()));

          agents.add(
            new TencentSecurityGroupCachingAgent(
              credential,
              objectMapper,
              registry,
              (region).getName()));

          agents.add(
            new TencentNetworkCachingAgent(
              credential,
              objectMapper,
              (region).getName()));

          agents.add(
            new TencentSubnetCachingAgent(
              credential,
              objectMapper,
              (region).getName()));

          agents.add(
            new TencentLoadBalancerInstanceStateCachingAgent(
              credential,
              objectMapper,
              region.getName()));
        });
    return agents;
  }

}
