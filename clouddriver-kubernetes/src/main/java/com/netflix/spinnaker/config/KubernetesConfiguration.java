/*
 * Copyright 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package com.netflix.spinnaker.config;

import com.netflix.spinnaker.clouddriver.kubernetes.config.KubernetesConfigurationProperties;
import com.netflix.spinnaker.clouddriver.kubernetes.health.KubernetesHealthIndicator;
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsProvider;
import com.netflix.spinnaker.clouddriver.security.ProviderVersion;
import com.netflix.spinnaker.grpc.CloudProviderGrpcClient;
import com.netflix.spinnaker.grpc.KubernetesGrpcAccount;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties
@EnableScheduling
@ConditionalOnProperty("kubernetes.enabled")
@ComponentScan({"com.netflix.spinnaker.clouddriver.kubernetes"})
@Slf4j
public class KubernetesConfiguration {

  @Value("${cd.coding.grpc.host:127.0.0.1}")
  private String host;

  @Value("${cd.coding.grpc.port:20153}")
  private int port;

  @Bean
  @ConfigurationProperties("kubernetes")
  public KubernetesConfigurationProperties kubernetesConfigurationProperties() {
    return new KubernetesConfigurationProperties();
  }

  @Bean
  public KubernetesHealthIndicator kubernetesHealthIndicator(
      AccountCredentialsProvider accountCredentialsProvider) {
    return new KubernetesHealthIndicator(accountCredentialsProvider);
  }

  public List<KubernetesConfigurationProperties.ManagedAccount> getAccounts() {
    List<KubernetesGrpcAccount> cloudProviders =
        new CloudProviderGrpcClient(host, port).getKubernetesAccounts();
    List<KubernetesConfigurationProperties.ManagedAccount> managedAccounts = new ArrayList<>();
    cloudProviders.forEach(
        cp -> {
          KubernetesConfigurationProperties.ManagedAccount managedAccount =
              new KubernetesConfigurationProperties.ManagedAccount();
          managedAccount.setName(cp.getName());
          managedAccount.setNamespaces(cp.getNamespaces());
          managedAccount.setKubeconfigContents(cp.getKubeconfigContents());
          managedAccount.setContext(cp.getContext());
          managedAccount.setProviderVersion(ProviderVersion.v2);
          managedAccount.setServiceAccount(cp.getServiceaccount());
          managedAccounts.add(managedAccount);
        });
    return managedAccounts;
  }
}
