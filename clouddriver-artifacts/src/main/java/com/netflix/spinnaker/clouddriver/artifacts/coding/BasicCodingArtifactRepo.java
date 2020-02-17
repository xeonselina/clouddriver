package com.netflix.spinnaker.clouddriver.artifacts.coding;

import lombok.Data;

@Data
public class BasicCodingArtifactRepo {

  private String name;
  private int id;
  private ArtifactRepoType artifactRepoType;

  public enum ArtifactRepoType {
    gitDepot,
    gitLabDepot,
    gitHubDepot,
    codingArtifacts,
    codingDockerRegister;

    private static final ArtifactRepoType[] types = ArtifactRepoType.values();

    public static ArtifactRepoType fromInteger(int i) {
      return types[i];
    }

    @Override
    public String toString() {
      return this.name().toLowerCase();
    }
  }
}
