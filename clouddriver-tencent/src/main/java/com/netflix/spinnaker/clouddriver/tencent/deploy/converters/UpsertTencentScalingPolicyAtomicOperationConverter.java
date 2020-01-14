package com.netflix.spinnaker.clouddriver.tencent.deploy.converters;

import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation;
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperations;
import com.netflix.spinnaker.clouddriver.security.AbstractAtomicOperationsCredentialsSupport;
import com.netflix.spinnaker.clouddriver.tencent.TencentOperation;
import com.netflix.spinnaker.clouddriver.tencent.deploy.description.UpsertTencentScalingPolicyDescription;
import com.netflix.spinnaker.clouddriver.tencent.deploy.ops.UpsertTencentScalingPolicyAtomicOperation;
import java.util.Map;
import org.springframework.stereotype.Component;

@TencentOperation(AtomicOperations.UPSERT_SCALING_POLICY)
@Component("upsertTencentScalingPolicyDescription")
public class UpsertTencentScalingPolicyAtomicOperationConverter
    extends AbstractAtomicOperationsCredentialsSupport {
  public AtomicOperation convertOperation(Map input) {
    return new UpsertTencentScalingPolicyAtomicOperation(convertDescription(input));
  }

  public UpsertTencentScalingPolicyDescription convertDescription(Map input) {
    return TencentAtomicOperationConverterHelper.convertDescription(
        input, this, UpsertTencentScalingPolicyDescription.class);
  }
}
