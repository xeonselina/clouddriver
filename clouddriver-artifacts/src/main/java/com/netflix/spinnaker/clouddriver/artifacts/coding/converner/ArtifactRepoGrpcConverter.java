package com.netflix.spinnaker.clouddriver.artifacts.coding.converner;

import com.netflix.spinnaker.clouddriver.artifacts.coding.BasicCodingArtifactRepo;
import java.util.List;
import net.coding.cd.proto.ArtifactsProto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ArtifactRepoGrpcConverter {
  ArtifactRepoGrpcConverter INSTANCE = Mappers.getMapper(ArtifactRepoGrpcConverter.class);

  default BasicCodingArtifactRepo proto2ArtifactRepo(ArtifactsProto.ArtifactRepo provider) {
    if (provider == null) {
      return null;
    }

    BasicCodingArtifactRepo artifactRepo = new BasicCodingArtifactRepo();
    artifactRepo.setArtifactRepoType(
        BasicCodingArtifactRepo.ArtifactRepoType.fromInteger(
            provider.getArtifactRepoType().getNumber()));
    artifactRepo.setId(provider.getId());
    artifactRepo.setName(provider.getName());

    return artifactRepo;
  }

  List<BasicCodingArtifactRepo> proto2ArtifactRepo(List<ArtifactsProto.ArtifactRepo> providers);
}
