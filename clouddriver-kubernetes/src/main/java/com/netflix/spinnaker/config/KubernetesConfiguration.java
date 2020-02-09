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
import com.netflix.spinnaker.grpc.client.CloudProviderGrpcClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties
@EnableScheduling
@ConditionalOnProperty("kubernetes.enabled")
@ComponentScan({"com.netflix.spinnaker.clouddriver.kubernetes"})
@Slf4j
public class KubernetesConfiguration {

  @Resource
  private CloudProviderGrpcClient cloudProviderGrpcClient;

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
    cloudProviderGrpcClient.getKubernetesAccounts().forEach(
      cp -> {
        KubernetesConfigurationProperties.ManagedAccount managedAccount =
          new KubernetesConfigurationProperties.ManagedAccount();
        managedAccount.setName(cp.getName());
        managedAccount.setNamespaces(cp.getNamespaces());
        managedAccount.setKubeconfigContents(cp.getKubeconfigContents());
        managedAccount.setContext(cp.getContext());
        managedAccount.setProviderVersion(ProviderVersion.v2);
        managedAccount.setServiceAccount(cp.getServiceaccount());

//        managedAccount.setOnlySpinnakerManaged(true);
//        managedAccount.setCacheThreads(8);
//        Permissions.Builder builder = new Permissions.Builder().add(Authorization.READ, "codingcorp:团队所有者")
//          .add(Authorization.WRITE, "codingcorp:团队所有者")
//          .add(Authorization.EXECUTE, "codingcorp:团队所有者");
//        managedAccount.setPermissions(builder);
        managedAccounts.add(managedAccount);
      });
    return managedAccounts;
  }
}
