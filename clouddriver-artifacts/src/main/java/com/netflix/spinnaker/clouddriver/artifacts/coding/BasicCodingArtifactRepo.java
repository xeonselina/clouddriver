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
    codingGeneric,
    codingDockerRegister,
    codingMaven,
    codingNpm,
    codingPypi,
    codingHelm,
    codingComposer,
    codingNuget,
    codingConan;
    private static final ArtifactRepoType[] types = ArtifactRepoType.values();

    public static ArtifactRepoType fromInteger(int i) {
      return types[i];
    }

    public static ArtifactRepoType fromArtifactType(Integer i) {
      return types[i + 3];
    }

    @Override
    public String toString() {
      return this.name().toLowerCase();
    }
  }
}
