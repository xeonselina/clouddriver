package com.netflix.spinnaker.config;

import com.netflix.spinnaker.cats.module.CatsModule;
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsRepository;
import com.netflix.spinnaker.clouddriver.tencent.config.TencentConfigurationProperties;
import com.netflix.spinnaker.clouddriver.tencent.provider.TencentInfrastructureProvider;
import com.netflix.spinnaker.clouddriver.tencent.provider.config.TencentInfrastructureProviderConfig;
import com.netflix.spinnaker.clouddriver.tencent.security.TencentCredentialsInitializer;
import com.netflix.spinnaker.grpc.client.CloudProviderGrpcClient;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
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

  @Resource private CloudProviderGrpcClient cloudProviderGrpcClient;

  @Bean
  public TencentCredentialsInitializer tencentCredentialsInitializer(
      AccountCredentialsRepository accountCredentialsRepository,
      TencentConfiguration tencentConfiguration,
      TencentInfrastructureProvider tencentInfrastructureProvider,
      CatsModule catsModule,
      TencentInfrastructureProviderConfig tencentInfrastructureProviderConfig) {
    return new TencentCredentialsInitializer(
        accountCredentialsRepository,
        tencentConfiguration,
        tencentInfrastructureProvider,
        catsModule,
        tencentInfrastructureProviderConfig);
  }

  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Bean
  @ConfigurationProperties("tencent")
  public TencentConfigurationProperties tencentConfigurationProperties() {
    return new TencentConfigurationProperties();
  }

  public List<TencentConfigurationProperties.ManagedAccount> getAccounts() {
    List<TencentConfigurationProperties.ManagedAccount> managedAccounts = new ArrayList<>();
    cloudProviderGrpcClient
        .getTencentAccounts()
        .forEach(
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
