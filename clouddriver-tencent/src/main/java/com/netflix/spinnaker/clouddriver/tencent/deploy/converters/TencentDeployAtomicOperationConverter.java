package com.netflix.spinnaker.clouddriver.tencent.deploy.converters;

import com.netflix.spinnaker.clouddriver.deploy.DeployAtomicOperation;
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation;
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperations;
import com.netflix.spinnaker.clouddriver.security.AbstractAtomicOperationsCredentialsSupport;
import com.netflix.spinnaker.clouddriver.tencent.TencentOperation;
import com.netflix.spinnaker.clouddriver.tencent.deploy.description.TencentDeployDescription;
import java.util.Map;
import org.springframework.stereotype.Component;

@TencentOperation(AtomicOperations.CREATE_SERVER_GROUP)
@Component("tencentDeployDescription")
public class TencentDeployAtomicOperationConverter
    extends AbstractAtomicOperationsCredentialsSupport {
  public AtomicOperation convertOperation(Map input) {
    return new DeployAtomicOperation(convertDescription(input));
  }

  public TencentDeployDescription convertDescription(Map input) {
    return TencentAtomicOperationConverterHelper.convertDescription(
        input, this, TencentDeployDescription.class);
  }
}
