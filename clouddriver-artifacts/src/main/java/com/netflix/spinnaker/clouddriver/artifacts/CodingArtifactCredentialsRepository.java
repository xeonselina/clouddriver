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

import static com.netflix.spinnaker.clouddriver.artifacts.coding.BasicCodingArtifactRepo.ArtifactRepoType.*;

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

  public List<ArtifactCredentials> getReposByAppId(int appId) {
    // 来自 coding 的 artifacts repo
    CodingArtifactRepoGrpcClient client = new CodingArtifactRepoGrpcClient();
    List<BasicCodingArtifactRepo> codingRepos = client.getArtifactRepos(appId);
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
                case codingArtifacts:
                  return new ArtifactRepoCredentials(
                      "coding_artifacts/object", it.getName(), it.getId());
                case codingDockerRegister:
                  return new ArtifactRepoCredentials("docker/image", it.getName(), it.getId());
                default:
                  throw new IllegalStateException("Unexpected value: " + it.getArtifactRepoType());
              }
            })
        .collect(Collectors.toList());
    // todo:
    // kubernetes，external docker registry, custom artifacts
  }

  public ArtifactCredentials getCredentials(String accountName, String type, int appId) {
    return this.getReposByAppId(appId).stream()
        .filter(it -> it.getName().equals(accountName) && it.handlesType(type))
        .findFirst()
        .orElse(null);
  }
}
