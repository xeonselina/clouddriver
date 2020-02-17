package com.netflix.spinnaker.clouddriver.artifacts.coding.client;

import com.netflix.spinnaker.clouddriver.artifacts.coding.BasicCodingArtifactRepo;
import com.netflix.spinnaker.clouddriver.artifacts.coding.converner.ArtifactRepoGrpcConverter;
import com.netflix.spinnaker.grpc.client.BaseClient;
import io.grpc.StatusRuntimeException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.coding.cd.proto.ArtifactsProto;
import net.coding.cd.proto.GrpcArtifactsServiceGrpc;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CodingArtifactRepoGrpcClient extends BaseClient {

  private GrpcArtifactsServiceGrpc.GrpcArtifactsServiceBlockingStub getStub() {
    return GrpcArtifactsServiceGrpc.newBlockingStub(openChannel());
  }

  public List<BasicCodingArtifactRepo> getArtifactRepos(int applicationId) {
    try {
      ArtifactsProto.GetArtifactReposRequest request =
          ArtifactsProto.GetArtifactReposRequest.newBuilder().setAppId(applicationId).build();
      ArtifactsProto.ArtifactReposResponse response = getStub().getArtifactRepos(request);
      return ArtifactRepoGrpcConverter.INSTANCE.proto2ArtifactRepo(response.getArtifactReposList());
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }
}
