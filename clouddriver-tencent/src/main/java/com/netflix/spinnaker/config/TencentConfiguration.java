package com.netflix.spinnaker.config;

import com.netflix.spectator.api.Registry;
import com.netflix.spinnaker.cats.module.CatsModule;
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsRepository;
import com.netflix.spinnaker.clouddriver.tencent.config.TencentConfigurationProperties;
import com.netflix.spinnaker.clouddriver.tencent.provider.TencentInfrastructureProvider;
import com.netflix.spinnaker.clouddriver.tencent.provider.config.TencentInfrastructureProviderConfig;
import com.netflix.spinnaker.clouddriver.tencent.security.TencentCredentialsInitializer;
import com.netflix.spinnaker.grpc.CloudProviderGrpcClient;
import com.netflix.spinnaker.grpc.TencentGrpcAccount;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties
@EnableScheduling
@ConditionalOnProperty("tencent.enabled")
@ComponentScan({"com.netflix.spinnaker.clouddriver.tencent"})
@Import({com.netflix.spinnaker.clouddriver.tencent.security.TencentCredentialsInitializer.class})
public class TencentConfiguration {

  @Value("${cd.coding.grpc.host:127.0.0.1}")
  private String host;

  @Value("${cd.coding.grpc.port:20153}")
  private int port;

  @Bean
  public TencentCredentialsInitializer tencentCredentialsInitializer(
      AccountCredentialsRepository accountCredentialsRepository,
      TencentConfiguration tencentConfiguration,
      TencentInfrastructureProvider tencentInfrastructureProvider,
      CatsModule catsModule,
      Registry registry,
      TencentInfrastructureProviderConfig tencentInfrastructureProviderConfig) {
    return new TencentCredentialsInitializer(
        accountCredentialsRepository,
        tencentConfiguration,
        tencentInfrastructureProvider,
        catsModule,
        registry,
        tencentInfrastructureProviderConfig);
  }

  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Bean
  @ConfigurationProperties("tencent")
  public TencentConfigurationProperties tencentConfigurationProperties() {
    TencentConfigurationProperties tencentConfigurationProperties =
        new TencentConfigurationProperties();
    tencentConfigurationProperties.setAccounts(getAccounts());
    return tencentConfigurationProperties;
  }

  public List<TencentConfigurationProperties.ManagedAccount> getAccounts() {
    List<TencentGrpcAccount> cloudProviders =
        new CloudProviderGrpcClient(host, port).getTencentAccounts();
    List<TencentConfigurationProperties.ManagedAccount> managedAccounts = new ArrayList<>();
    cloudProviders.forEach(
        cp -> {
          TencentConfigurationProperties.ManagedAccount managedAccount =
              new TencentConfigurationProperties.ManagedAccount();
          managedAccount.setName(cp.getName());
          managedAccount.setSecretId(cp.getSecretId());
          managedAccount.setSecretKey(cp.getSecretKey());
          managedAccount.setRegions(cp.getRegions());
          managedAccounts.add(managedAccount);
        });
    return managedAccounts;
  }
}
