/*
 * Copyright 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.netflix.spinnaker.clouddriver.kubernetes.v2.controllers;

import com.netflix.spinnaker.grpc.CloudProviderGrpcClient;
import com.netflix.spinnaker.grpc.KubernetesGrpcAccount;
import com.netflix.spinnaker.grpc.TencentGrpcAccount;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/marlon/test")
public class TestController {

  @Value("${cd.coding.grpc.host:127.0.0.1}")
  private String host;

  @Value("${cd.coding.grpc.port:20153}")
  private int port;

  @RequestMapping("k8s")
  public List<KubernetesGrpcAccount> getKubernetes() {
    return new CloudProviderGrpcClient(host, port).getKubernetesAccounts();
  }

  @RequestMapping("tencent")
  public List<TencentGrpcAccount> getTencent() {
    return new CloudProviderGrpcClient(host, port).getTencentAccounts();
  }
}
