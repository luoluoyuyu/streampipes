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
package org.apache.streampipes.connect.management.util;

import org.apache.streampipes.commons.exceptions.NoServiceEndpointsAvailableException;
import org.apache.streampipes.manager.execution.endpoint.ExtensionsServiceEndpointGenerator;
import org.apache.streampipes.manager.loadbalance.LoadManager;
import org.apache.streampipes.manager.loadbalance.ResourceUnitGenerator;
import org.apache.streampipes.model.connect.adapter.AdapterDescription;
import org.apache.streampipes.model.extensions.svcdiscovery.SpServiceRegistration;
import org.apache.streampipes.model.loadbalancer.ResourceUnit;
import org.apache.streampipes.svcdiscovery.api.model.SpServiceUrlProvider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class WorkerPaths {

  private static final String WorkerMainPath = "/api/v1/worker";

  public static String getStreamInvokePath() {
    return WorkerMainPath + "/stream/invoke";
  }

  public static String getStreamStopPath() {
    return WorkerMainPath + "/stream/stop";
  }

  public static String getSetInvokePath() {
    return WorkerMainPath + "/set/invoke";
  }

  public static String getSetStopPath() {
    return WorkerMainPath + "/set/stop";
  }

  public static String getRunningAdaptersPath() {
    return WorkerMainPath + "/running";
  }

  public static String getRuntimeResolvablePath(String elementId) {
    return WorkerMainPath + "/resolvable/" + elementId + "/configurations";
  }

  public static String getGuessSchemaPath() {
    return WorkerMainPath + "/guess/schema";
  }

  public static String findEndpointUrl(AdapterDescription appId) throws NoServiceEndpointsAvailableException, URISyntaxException {
    SpServiceUrlProvider serviceUrlProvider = SpServiceUrlProvider.ADAPTER;
    Map<ResourceUnit<AdapterDescription>, List<SpServiceRegistration>> resourceUnit  = ResourceUnitGenerator.unitGeneration(appId);
    SpServiceRegistration spServiceRegistration=null;
    for(Map.Entry<ResourceUnit<AdapterDescription>, List<SpServiceRegistration>> e : resourceUnit.entrySet()) {
      spServiceRegistration=LoadManager.allocation(e.getKey(),e.getValue());
      e.getKey().setServiceId(spServiceRegistration.getSvcId());
    }
    String endpointUrl = serviceUrlProvider.getInvocationUrl(spServiceRegistration.getServiceUrl(),appId.getAppId());
    URI uri = new URI(endpointUrl);
    return uri.getScheme() + "://" + uri.getAuthority();
  }


}
