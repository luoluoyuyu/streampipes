/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.streampipes.integration.containers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

import static java.util.Arrays.asList;

public class RocketMQContainer extends GenericContainer<RocketMQDevContainer> {

  private static final String DEFAULT_TAG = "latest";
  private static final int defaultBrokerPermission = 6;
  public static final int NAME_SRV_PORT = 9876;
  public static final int BROKER_PORT = 10911;
  public static final int BROKER_INTERNAL_PORT = 10909;

  private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("apache/rocketmq:" + DEFAULT_TAG);


  public RocketMQContainer() {
    super(DEFAULT_IMAGE_NAME);
    withExposedPorts(NAME_SRV_PORT, BROKER_PORT, BROKER_INTERNAL_PORT);
  }

  @Override
  protected void configure() {
    withCommand("sh", "-c", "#!/bin/bash\n"
        + "./mqnamesrv &\n"
        + "./mqproxy &\n"
        + "./mqbroker -n localhost:" + NAME_SRV_PORT );
  }


  @Override
  protected void containerIsStarted(InspectContainerResponse containerInfo) {
    String brokerIP = getHost();
    int mappedBrokerPort = getMappedPort(BROKER_PORT);
    String command = String.join(
        " && ",
         asList(
            updateBrokerConfig("brokerIP1", brokerIP),
            updateBrokerConfig("listenPort", mappedBrokerPort),
            updateBrokerConfig("brokerPermission", defaultBrokerPermission)
            )
        );
    execInContainerAndCheckError("/bin/sh", "-c", command);
  }

  private String updateBrokerConfig(String key, Object val) {
    return String.format(
        "./mqadmin updateBrokerConfig -b localhost:%d -k %s -v %s",
        BROKER_PORT, key, val
        );
  }

  private void execInContainerAndCheckError(String... command) {
    ExecResult result;
    try {
      result = execInContainer(command);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Error executing command in container", e);
    }

    if (result.getExitCode() != 0) {
      throw new IllegalStateException("Command execution failed: " + result);
    }
  }

  public String getBrokerHost() {
    return getHost();
  }

  public Integer getBrokerPort() {
    return getMappedPort(NAME_SRV_PORT);
  }

}
