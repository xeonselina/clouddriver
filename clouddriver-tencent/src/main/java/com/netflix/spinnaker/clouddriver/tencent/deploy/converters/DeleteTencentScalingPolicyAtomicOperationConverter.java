package com.netflix.spinnaker.clouddriver.tencent.deploy.converters;

import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation;
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperations;
import com.netflix.spinnaker.clouddriver.security.AbstractAtomicOperationsCredentialsSupport;
import com.netflix.spinnaker.clouddriver.tencent.TencentOperation;
import com.netflix.spinnaker.clouddriver.tencent.deploy.description.DeleteTencentScalingPolicyDescription;
import com.netflix.spinnaker.clouddriver.tencent.deploy.ops.DeleteTencentScalingPolicyAtomicOperation;
import java.util.Map;
import org.springframework.stereotype.Component;

@TencentOperation(AtomicOperations.DELETE_SCALING_POLICY)
@Component("deleteTencentScalingPolicyDescription")
public class DeleteTencentScalingPolicyAtomicOperationConverter
    extends AbstractAtomicOperationsCredentialsSupport {
  @Override
  public DeleteTencentScalingPolicyDescription convertDescription(Map input) {
    return TencentAtomicOperationConverterHelper.convertDescription(
        input, this, DeleteTencentScalingPolicyDescription.class);
  }

  @Override
  public AtomicOperation convertOperation(Map input) {
    return new DeleteTencentScalingPolicyAtomicOperation(convertDescription(input));
  }
}
