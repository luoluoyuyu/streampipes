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

package org.apache.streampipes.connect.shared.preprocessing.transform.schema;

import org.apache.streampipes.connect.shared.preprocessing.SupportsNestedTransformationRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @deprecated The functionlality to add nested rules was removed in version 0.97.0 form the UI
 * For the next release we can also remove the functionality from the backend
 */
@Deprecated(since = "0.97.0", forRemoval = true)
public class CreateNestedTransformationRule extends SupportsNestedTransformationRule {

  private final List<String> key;

  public CreateNestedTransformationRule(List<String> key) {
    this.key = key;
  }

  @Override
  protected List<String> getEventKeys() {
    return key;
  }

  @Override
  protected void applyTransformation(Map<String, Object> event, List<String> eventKeys) {
    event.put(eventKeys.get(0), new HashMap<>());
  }
}
