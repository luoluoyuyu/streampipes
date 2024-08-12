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

package org.apache.streampipes.integration.adapters;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.streampipes.commons.exceptions.connect.AdapterException;
import org.apache.streampipes.extensions.api.connect.IAdapterConfiguration;
import org.apache.streampipes.extensions.api.connect.StreamPipesAdapter;
import org.apache.streampipes.extensions.connectors.rocketmq.adapter.RocketMQProtocol;
import org.apache.streampipes.integration.containers.RocketMQContainer;
import org.apache.streampipes.integration.containers.RocketMQDevContainer;
import org.apache.streampipes.integration.utils.Utils;
import org.apache.streampipes.manager.template.AdapterTemplateHandler;
import org.apache.streampipes.model.staticproperty.StaticPropertyAlternatives;
import org.apache.streampipes.model.template.PipelineElementTemplate;
import org.apache.streampipes.model.template.PipelineElementTemplateConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RocketMQAdapterTester extends AdapterTesterBase {
  RocketMQContainer rocketMQContainer;
  private static final String TOPIC = "test-topic";
  private static final String PRODUCER_GROUP = "test";

  @Override
  public void startAdapterService() {
    if (Objects.equals(System.getenv("TEST_MODE"), "dev")) {
      rocketMQContainer = new RocketMQDevContainer();
    } else {
      rocketMQContainer = new RocketMQDevContainer();
    }
    rocketMQContainer.start();
  }

  @Override
  public IAdapterConfiguration prepareAdapter() {
    IAdapterConfiguration configuration = new RocketMQProtocol().declareConfig();

    Map<String, PipelineElementTemplateConfig> configs = new HashMap<>();
    configs.put(RocketMQProtocol.ENDPOINT_KEY,
        new PipelineElementTemplateConfig(true, true,
            rocketMQContainer.getBrokerHost() + ":" + rocketMQContainer.getBrokerPort()));
    configs.put(RocketMQProtocol.TOPIC_KEY,
        new PipelineElementTemplateConfig(true, false, TOPIC));
    configs.put(RocketMQProtocol.CONSUMER_GROUP_KEY,
        new PipelineElementTemplateConfig(true, false, PRODUCER_GROUP));

    var template = new PipelineElementTemplate("name", "description", configs);

    var desc = new AdapterTemplateHandler(template,
                        configuration.getAdapterDescription(),
                        true)
                        .applyTemplateOnPipelineElement();

    ((StaticPropertyAlternatives) (desc)
          .getConfig()
          .get(3))
          .getAlternatives()
          .get(0)
          .setSelected(true);

    return configuration;
  }

  @Override
  public StreamPipesAdapter getAdapterInstance() {
    return new RocketMQProtocol();
  }

  @Override
  public List<Map<String, Object>> getTestEvents() {
    return Utils.getSimpleTestEvents();
  }

  @Override
  public void publishEvents(List<Map<String, Object>> events) {
    final DefaultMQProducer producer = new DefaultMQProducer(PRODUCER_GROUP);
    producer.setNamesrvAddr(rocketMQContainer.getBrokerHost() + ":" + rocketMQContainer.getBrokerPort());
    try {
      producer.start();
      var objectMapper = new ObjectMapper();
      events.forEach(event -> {
        try {
          var serializedEvent = objectMapper.writeValueAsBytes(event);
          producer.send(new Message(TOPIC, serializedEvent));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      producer.shutdown();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    if (rocketMQContainer != null) {
      rocketMQContainer.stop();
    }
    try {
      stopAdapter();
    } catch (AdapterException e) {
      throw new RuntimeException(e);
    }
  }
}
