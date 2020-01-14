package com.netflix.spinnaker.clouddriver.tencent.model.loadbalance;

import lombok.Data;

import java.util.List;

@Data
public class TencentLoadBalancerRule {
  private String locationId;
  private String domain;
  private String url;
  private Integer sessionExpireTime;
  private TencentLoadBalancerHealthCheck healthCheck;
  private TencentLoadBalancerCertificate certificate;
  private String scheduler;
  private List<TencentLoadBalancerTarget> targets;
}
