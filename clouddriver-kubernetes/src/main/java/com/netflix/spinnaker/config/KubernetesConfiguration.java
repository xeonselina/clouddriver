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
import com.netflix.spinnaker.grpc.client.CloudAccountGrpcClient;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

  @Resource private CloudAccountGrpcClient cloudAccountGrpcClient;

  @Bean
  public KubernetesConfigurationProperties kubernetesConfigurationProperties() {
    return new KubernetesConfigurationProperties();
  }

  @Bean
  public KubernetesHealthIndicator kubernetesHealthIndicator(
      AccountCredentialsProvider accountCredentialsProvider) {
    return new KubernetesHealthIndicator(accountCredentialsProvider);
  }

  public List<KubernetesConfigurationProperties.ManagedAccount> getAccounts() {
    List<KubernetesConfigurationProperties.ManagedAccount> managedAccounts = new ArrayList<>();
    cloudAccountGrpcClient
        .getKubernetesAccounts()
        .forEach(
            account -> {
              KubernetesConfigurationProperties.ManagedAccount managedAccount =
                  new KubernetesConfigurationProperties.ManagedAccount();
              managedAccount.setName(account.getName());
              managedAccount.setNamespaces(account.getNamespaces());
              managedAccount.setKubeconfigContents(account.getKubeconfigContents());
              managedAccount.setContext(account.getContext());
              managedAccount.setProviderVersion(ProviderVersion.v2);
              managedAccount.setServiceAccount(account.getServiceaccount());
              managedAccount.setPermissions(account.getPermissions());
              managedAccounts.add(managedAccount);
            });
    return managedAccounts;
  }
}
