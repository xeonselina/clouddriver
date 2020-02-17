/*
 * Copyright 2017 Armory, Inc.
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

package com.netflix.spinnaker.clouddriver.artifacts.coding.artifactrepo;

import com.netflix.spinnaker.clouddriver.artifacts.config.ArtifactCredentials;
import com.netflix.spinnaker.kork.artifacts.model.Artifact;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.NotSupportedException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// 包括 coding 制品库 和 coding docker 仓库
public class ArtifactRepoCredentials implements ArtifactCredentials {
  @Getter private final String name;
  @Getter private final List<String> types;
  @Getter private final int repoId;

  public ArtifactRepoCredentials(String type, String repoName, int repoId) {
    this.name = repoName;
    this.types = Collections.singletonList(type);
    this.repoId = repoId;
  }

  @Override
  public InputStream download(Artifact artifact) throws IOException {
    throw new NotSupportedException("not support");
  }
}
