package com.netflix.spinnaker.clouddriver.tencent.provider.view

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.cats.cache.Cache
import com.netflix.spinnaker.cats.cache.CacheData
import com.netflix.spinnaker.cats.cache.RelationshipCacheFilter
import com.netflix.spinnaker.clouddriver.model.InstanceProvider
import com.netflix.spinnaker.clouddriver.tencent.TencentCloudProvider
import com.netflix.spinnaker.clouddriver.tencent.model.TencentInstance
import com.netflix.spinnaker.clouddriver.tencent.model.TencentTargetHealth
import com.netflix.spinnaker.clouddriver.tencent.model.loadbalance.TencentLoadBalancerTargetHealth

import static com.netflix.spinnaker.clouddriver.tencent.cache.Keys.Namespace.*

import com.netflix.spinnaker.clouddriver.tencent.cache.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TencentInstanceProvider implements InstanceProvider<TencentInstance, String> {
  final String cloudProvider = TencentCloudProvider.ID

  @Autowired
  ObjectMapper objectMapper

  @Autowired
  TencentCloudProvider tencentCloudProvider

  @Autowired
  Cache cacheView

  @Override
  TencentInstance getInstance(String account, String region, String id) {
    def key = Keys.getInstanceKey id, account, region

    cacheView.getAll(
      INSTANCES.ns,
      [key],
      RelationshipCacheFilter.include(
        LOAD_BALANCERS.ns,
        SERVER_GROUPS.ns,
        CLUSTERS.ns
      )
    )?.findResult { CacheData cacheData ->
      instanceFromCacheData(account, region, cacheData)
    }
  }

  TencentInstance instanceFromCacheData(String account, String region, CacheData cacheData) {
    TencentInstance instance = objectMapper.convertValue cacheData.attributes.instance, TencentInstance

    def serverGroupName = instance.serverGroupName
    def serverGroupCache = cacheView.get(SERVER_GROUPS.ns, Keys.getServerGroupKey(serverGroupName, account, region))
    def asgInfo = serverGroupCache?.attributes?.asg as Map
    def lbInfo = asgInfo?.get("forwardLoadBalancerSet") as List
    if (lbInfo) {
      def lbId = lbInfo[0]["loadBalancerId"] as String
      def listenerId = lbInfo[0]["listenerId"] as String
      def lbHealthKey = Keys.getTargetHealthKey(
        lbId, listenerId, '', instance.name, account, region)
      def lbHealthCache = cacheView.get(HEALTH_CHECKS.ns, lbHealthKey)
      def loadBalancerTargetHealth = lbHealthCache?.attributes?.targetHealth as TencentLoadBalancerTargetHealth
      if (loadBalancerTargetHealth) {
        instance.targetHealth = new TencentTargetHealth(loadBalancerTargetHealth.healthStatus)
        def healthStatus = instance.targetHealth.targetHealthStatus
        instance.targetHealth.loadBalancers.add(new TencentTargetHealth.LBHealthSummary(
          loadBalancerName: lbId,
          state: healthStatus.toServiceStatus()
        ))
      } else {
        // if server group has lb, but can't get lb health check result for instance in server group
        // assume the target health check result is UNKNOWN
        instance.targetHealth = new TencentTargetHealth()
      }
    }
    instance
  }

  @Override
  String getConsoleOutput(String account, String region, String id) {
    return null
  }
}
