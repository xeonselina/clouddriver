package com.netflix.spinnaker.clouddriver.tencent.security;

import com.netflix.spinnaker.cats.agent.Agent;
import com.netflix.spinnaker.cats.module.CatsModule;
import com.netflix.spinnaker.cats.thread.NamedThreadFactory;
import com.netflix.spinnaker.clouddriver.security.AccountCredentials;
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsRepository;
import com.netflix.spinnaker.clouddriver.security.CredentialsInitializerSynchronizable;
import com.netflix.spinnaker.clouddriver.security.ProviderUtils;
import com.netflix.spinnaker.clouddriver.tencent.config.TencentConfigurationProperties;
import com.netflix.spinnaker.clouddriver.tencent.provider.TencentInfrastructureProvider;
import com.netflix.spinnaker.clouddriver.tencent.provider.config.TencentInfrastructureProviderConfig;
import com.netflix.spinnaker.config.TencentConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@Slf4j
public class TencentCredentialsInitializer implements CredentialsInitializerSynchronizable {

  private final TencentConfiguration tencentConfiguration;
  private final AccountCredentialsRepository accountCredentialsRepository;
  private final TencentInfrastructureProvider tencentInfrastructureProvider;
  private final CatsModule catsModule;
  private final TencentInfrastructureProviderConfig tencentInfrastructureProviderConfig;

  public TencentCredentialsInitializer(
      AccountCredentialsRepository accountCredentialsRepository,
      TencentConfiguration tencentConfiguration,
      TencentInfrastructureProvider tencentInfrastructureProvider,
      CatsModule catsModule,
      TencentInfrastructureProviderConfig tencentInfrastructureProviderConfig) {
    this.accountCredentialsRepository = accountCredentialsRepository;
    this.tencentConfiguration = tencentConfiguration;
    this.tencentInfrastructureProvider = tencentInfrastructureProvider;
    this.tencentInfrastructureProviderConfig = tencentInfrastructureProviderConfig;
    this.catsModule = catsModule;

    ScheduledExecutorService poller =
        Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory(TencentCredentialsInitializer.class.getSimpleName()));

    Integer delay =
        Optional.ofNullable(System.getenv("tencent.sync.delay")).map(Integer::valueOf).orElse(30);

    poller.scheduleWithFixedDelay(this::synchronize, 5, delay, TimeUnit.SECONDS);
  }

  @Override
  @PostConstruct
  public void synchronize() {
    List<String> deletedAccountNames =
        getDeletedAccountNames(accountCredentialsRepository, tencentConfiguration);
    List<String> changedAccountNames =
        synchronizeRepository(tencentConfiguration.getAccounts(), deletedAccountNames);
    synchronizeAgentCache(changedAccountNames, deletedAccountNames);
  }

  private List<String> synchronizeRepository(
      List<TencentConfigurationProperties.ManagedAccount> accounts,
      List<String> deletedAccountNames) {
    List<String> changedAccountNames = new ArrayList<>();
    deletedAccountNames.forEach(accountCredentialsRepository::delete);
    accounts.forEach(
        managedAccount -> {
          final String environment = managedAccount.getEnvironment();
          final String type = managedAccount.getAccountType();
          TencentNamedAccountCredentials credentials =
              new TencentNamedAccountCredentials(
                  managedAccount.getName(),
                  !StringUtils.isEmpty(environment) ? environment : managedAccount.getName(),
                  !StringUtils.isEmpty(type) ? type : managedAccount.getName(),
                  managedAccount.getSecretId(),
                  managedAccount.getSecretKey(),
                  managedAccount.getRegions(),
                  managedAccount.getPermissions());
          AccountCredentials existingCredentials =
              accountCredentialsRepository.getOne(credentials.getName());
          if (existingCredentials != null) {
            if (!existingCredentials.equals(credentials)) {
              accountCredentialsRepository.save(managedAccount.getName(), credentials);
              changedAccountNames.add(managedAccount.getName());
            }
          } else {
            accountCredentialsRepository.save(managedAccount.getName(), credentials);
          }
        });

    return changedAccountNames;
  }

  private void synchronizeAgentCache(
      List<String> changedAccountNames, List<String> deletedAccountNames) {
    ProviderUtils.unscheduleAndDeregisterAgents(changedAccountNames, catsModule);
    ProviderUtils.unscheduleAndDeregisterAgents(deletedAccountNames, catsModule);
    List<Agent> addedAgents = getAddedAgents();
    tencentInfrastructureProvider.getAgents().addAll(addedAgents);
    ProviderUtils.rescheduleAgents(tencentInfrastructureProvider, addedAgents);
  }

  private List<String> getDeletedAccountNames(
      AccountCredentialsRepository accountCredentialsRepository,
      TencentConfiguration tencentConfiguration) {
    List<String> existingNames =
        accountCredentialsRepository.getAll().stream()
            .filter(c -> "tencent".equalsIgnoreCase(c.getCloudProvider()))
            .map(AccountCredentials::getName)
            .collect(Collectors.toList());
    List<String> newNames =
        tencentConfiguration.getAccounts().stream()
            .map(TencentConfigurationProperties.ManagedAccount::getName)
            .collect(Collectors.toList());
    return existingNames.stream()
        .filter(name -> !newNames.contains(name))
        .collect(Collectors.toList());
  }

  private List<Agent> getAddedAgents() {
    Set<String> existingAgentAccountNames =
        ProviderUtils.getScheduledAccounts(tencentInfrastructureProvider);
    Set<TencentNamedAccountCredentials> allAccounts =
        ProviderUtils.buildThreadSafeSetOfAccounts(
            accountCredentialsRepository, TencentNamedAccountCredentials.class);
    List<Agent> agentList = new ArrayList<>();
    allAccounts.stream()
        .filter(account -> !existingAgentAccountNames.contains(account.getName()))
        .map(tencentInfrastructureProviderConfig::syncAgents)
        .forEach(e -> agentList.addAll(new ArrayList<>(e)));
    return agentList;
  }
}
