package com.netflix.spinnaker.config;

import com.netflix.spinnaker.clouddriver.grpc.CloudProviderGrpcClient;
import com.netflix.spinnaker.clouddriver.kubernetes.config.KubernetesConfigurationProperties;
import com.netflix.spinnaker.clouddriver.kubernetes.health.KubernetesHealthIndicator;
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsProvider;
import com.netflix.spinnaker.clouddriver.security.ProviderVersion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.coding.e.proto.CloudProviderProto;
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
public class KubernetesConfiguration {

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

  public List<KubernetesConfigurationProperties.ManagedAccount> getAccount() {
    List<CloudProviderProto.CloudProvider> cloudProviders =
        new CloudProviderGrpcClient().doExecute();
    List<KubernetesConfigurationProperties.ManagedAccount> managedAccounts = new ArrayList<>();
    cloudProviders.forEach(
        cp -> {
          KubernetesConfigurationProperties.ManagedAccount managedAccount =
              new KubernetesConfigurationProperties.ManagedAccount();
          managedAccount.setName(cp.getName());
          managedAccount.setNamespaces(Collections.singletonList(cp.getNamespaces()));
          managedAccount.setKubeconfigContents(cp.getKubeconfigContents());
          managedAccount.setContext(cp.getContext());
          managedAccount.setProviderVersion(ProviderVersion.v2);
          managedAccount.setServiceAccount(cp.getServiceaccount());
          managedAccounts.add(managedAccount);
        });
    return managedAccounts;
  }
}
