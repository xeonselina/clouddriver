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

package com.netflix.spinnaker.clouddriver.artifacts;

import com.netflix.spinnaker.clouddriver.artifacts.coding.BasicCodingArtifactRepo;
import com.netflix.spinnaker.clouddriver.artifacts.coding.artifactrepo.ArtifactRepoCredentials;
import com.netflix.spinnaker.clouddriver.artifacts.coding.client.CodingArtifactRepoGrpcClient;
import com.netflix.spinnaker.clouddriver.artifacts.coding.git.GitArtifactCredentials;
import com.netflix.spinnaker.clouddriver.artifacts.config.ArtifactCredentials;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CodingArtifactCredentialsRepository {

  public List<ArtifactCredentials> getReposByApp(String appName) {
    // 来自 coding 的 artifacts repo
    CodingArtifactRepoGrpcClient client = new CodingArtifactRepoGrpcClient();
    List<BasicCodingArtifactRepo> codingRepos = client.getArtifactRepos(appName);
    return codingRepos.stream()
        .map(
            it -> {
              switch (it.getArtifactRepoType()) {
                case gitDepot:
                  return new GitArtifactCredentials("coding_git/file", it.getName(), it.getId());
                case gitLabDepot:
                  return new GitArtifactCredentials("gitlab/file", it.getName(), it.getId());
                case gitHubDepot:
                  return new GitArtifactCredentials("github/file", it.getName(), it.getId());
                case codingComposer:
                  return new ArtifactRepoCredentials(
                      "coding_composer/object", it.getName(), it.getId());
                case codingPypi:
                  return new ArtifactRepoCredentials(
                      "coding_pypi/object", it.getName(), it.getId());
                case codingGeneric:
                  return new ArtifactRepoCredentials(
                      "coding_generic/object", it.getName(), it.getId());
                case codingDockerRegister:
                  return new ArtifactRepoCredentials("docker/image", it.getName(), it.getId());
                case codingMaven:
                  return new ArtifactRepoCredentials(
                      "coding_maven/object", it.getName(), it.getId());
                case codingNpm:
                  return new ArtifactRepoCredentials("coding_npm/object", it.getName(), it.getId());
                case codingHelm:
                  return new ArtifactRepoCredentials(
                      "coding_helm/object", it.getName(), it.getId());
                case codingNuget:
                  return new ArtifactRepoCredentials(
                      "coding_nuget/object", it.getName(), it.getId());
                case codingConan:
                  return new ArtifactRepoCredentials(
                      "coding_conan/object", it.getName(), it.getId());
                default:
                  throw new IllegalStateException("Unexpected value: " + it.getArtifactRepoType());
              }
            })
        .collect(Collectors.toList());
    // todo:
    // kubernetes，external docker registry, custom artifacts
  }

  public ArtifactCredentials getCredentials(String accountName, String type, String appName) {
    return this.getReposByApp(appName).stream()
        .filter(it -> it.getName().equals(accountName) && it.handlesType(type))
        .findFirst()
        .orElse(null);
  }
}
