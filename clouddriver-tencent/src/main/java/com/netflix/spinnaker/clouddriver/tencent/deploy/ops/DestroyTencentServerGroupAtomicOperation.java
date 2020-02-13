package com.netflix.spinnaker.clouddriver.tencent.deploy.ops;

import static java.lang.Thread.sleep;

import com.netflix.spinnaker.clouddriver.data.task.Task;
import com.netflix.spinnaker.clouddriver.data.task.TaskRepository;
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation;
import com.netflix.spinnaker.clouddriver.tencent.client.AutoScalingClient;
import com.netflix.spinnaker.clouddriver.tencent.client.CloudVirtualMachineClient;
import com.netflix.spinnaker.clouddriver.tencent.deploy.description.DestroyTencentServerGroupDescription;
import com.netflix.spinnaker.clouddriver.tencent.exception.TencentOperationException;
import com.netflix.spinnaker.clouddriver.tencent.model.TencentServerGroup;
import com.netflix.spinnaker.clouddriver.tencent.provider.view.TencentClusterProvider;
import com.tencentcloudapi.as.v20180419.models.Activity;
import com.tencentcloudapi.as.v20180419.models.AutoScalingGroup;
import com.tencentcloudapi.as.v20180419.models.DescribeAutoScalingActivitiesResponse;
import com.tencentcloudapi.as.v20180419.models.Instance;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

@Data
@Slf4j
public class DestroyTencentServerGroupAtomicOperation implements AtomicOperation<Void> {
  public DestroyTencentServerGroupAtomicOperation(
      DestroyTencentServerGroupDescription description) {
    this.description = description;
  }

  @Override
  public Void operate(List priorOutputs) {
    log.info("come in to DestoryTencentServerGroup operate");
    // 1. detach all instances from asg
    // 2. terminate detached instances
    // 3. delete asg
    // 4. delete asc
    getTask()
        .updateStatus(
            BASE_PHASE,
            "Initializing destroy server group "
                + getDescription().getServerGroupName()
                + " in "
                + getDescription().getRegion()
                + "...");
    String region = description.getRegion();
    String accountName = description.getAccountName();
    String serverGroupName = description.getServerGroupName();

    AutoScalingClient client =
        new AutoScalingClient(
            description.getCredentials().getCredentials().getSecretId(),
            description.getCredentials().getCredentials().getSecretKey(),
            region);

    CloudVirtualMachineClient cvmClient =
        new CloudVirtualMachineClient(
            description.getCredentials().getCredentials().getSecretId(),
            description.getCredentials().getCredentials().getSecretKey(),
            region);

    getTask().updateStatus(BASE_PHASE, "Start destroy server group " + serverGroupName);
    TencentServerGroup serverGroup =
        tencentClusterProvider.getServerGroup(accountName, region, serverGroupName, false);

    log.info("serverGroup is {}", serverGroup);
    if (serverGroup != null) {
      Optional<AutoScalingGroup> asg =
          Optional.ofNullable(getAutoScalingGroup(client, serverGroupName));
      String asgId = asg.map(it -> it.getAutoScalingGroupId()).orElse(null);
      String ascId = asg.map(it -> it.getLaunchConfigurationId()).orElse(null);
      log.info("asgId is {}, ascId is {}", asgId, ascId);
      List<Instance> instances = client.getAutoScalingInstances(asgId);
      List<String> instanceIds =
          instances.stream().map(it -> it.getInstanceId()).collect(Collectors.toList());

      getTask()
          .updateStatus(
              BASE_PHASE,
              "Server group "
                  + serverGroupName
                  + " is related to auto scaling group "
                  + asgId
                  + " and launch configuration "
                  + ascId
                  + ".");

      log.info("instanceIds is {}", instanceIds);

      if (!CollectionUtils.isEmpty(instanceIds)) {
        int maxQueryTime = 10000;
        getTask().updateStatus(BASE_PHASE, "Will detach $instanceIds from " + asgId);
        String activityId = client.detachInstances(asgId, instanceIds).getActivityId();
        log.info("detachInstances activityId is {}", activityId);

        for (int i = 0; i < maxQueryTime; i++) {
          DescribeAutoScalingActivitiesResponse response =
              client.describeAutoScalingActivities(activityId);
          log.info("describeAutoScalingActivities response {}", response);
          if (!ArrayUtils.isEmpty(response.getActivitySet())) {
            Activity activity = response.getActivitySet()[0];
            String activity_status = activity.getStatusCode();

            if (activity_status.equals("SUCCESSFUL")) {
              log.info("detach activity is done");
              break;
            } else if (activity_status.equals("RUNNING") || activity_status.equals("INIT")) {
              log.info("detach activity is running");
            } else {
              log.error("detach activity is cancelled or failed");
              throw new TencentOperationException("detach activity is cancelled or failed");
            }
            try {
              sleep(1000);
            } catch (InterruptedException e) {
              e.printStackTrace();
              throw new TencentOperationException("sleep error", e);
            }
          } else {
            log.warn("found no activity");
          }
        }

        getTask()
            .updateStatus(BASE_PHASE, "Detach activity has finished, will start terminate soon.");
        log.info("detach activity finish ");
        cvmClient.terminateInstances(instanceIds);
        getTask().updateStatus(BASE_PHASE, "$instanceIds are terminaing.");
      }

      getTask().updateStatus(BASE_PHASE, "Deleting auto scaling group " + asgId + "...");
      client.deleteAutoScalingGroup(asgId);
      getTask().updateStatus(BASE_PHASE, "Auto scaling group " + asgId + " is deleted.");

      getTask().updateStatus(BASE_PHASE, "Deleting launch configuration " + ascId + "...");
      client.deleteLaunchConfiguration(ascId);
      getTask().updateStatus(BASE_PHASE, "Launch configuration " + ascId + " is deleted.");

      getTask().updateStatus(BASE_PHASE, "Complete destroy server group " + serverGroupName + ".");
    } else {
      getTask().updateStatus(BASE_PHASE, "Server group " + serverGroupName + " is not found.");
    }

    getTask().updateStatus(BASE_PHASE, "Complete destroy server group. ");
    return null;
  }

  private AutoScalingGroup getAutoScalingGroup(AutoScalingClient client, String serverGroupName) {
    List<AutoScalingGroup> asgs = client.getAutoScalingGroupsByName(serverGroupName);
    if (!CollectionUtils.isEmpty(asgs)) {
      AutoScalingGroup asg = asgs.get(0);
      String asgId = asg.getAutoScalingGroupId();
      getTask()
          .updateStatus(
              BASE_PHASE,
              "Server group " + serverGroupName + "\'s auto scaling group id is " + asgId);
      return asg;
    } else {
      getTask().updateStatus(BASE_PHASE, "Server group " + serverGroupName + " is not found.");
      return null;
    }
  }

  private static Task getTask() {
    return TaskRepository.threadLocalTask.get();
  }

  private static final String BASE_PHASE = "DESTROY_SERVER_GROUP";
  private DestroyTencentServerGroupDescription description;
  @Autowired private TencentClusterProvider tencentClusterProvider;
}
