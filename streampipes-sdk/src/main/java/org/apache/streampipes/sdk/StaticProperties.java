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

package org.apache.streampipes.sdk;

import org.apache.streampipes.model.schema.PropertyScope;
import org.apache.streampipes.model.staticproperty.CodeInputStaticProperty;
import org.apache.streampipes.model.staticproperty.CollectionStaticProperty;
import org.apache.streampipes.model.staticproperty.FileStaticProperty;
import org.apache.streampipes.model.staticproperty.FreeTextStaticProperty;
import org.apache.streampipes.model.staticproperty.MappingPropertyUnary;
import org.apache.streampipes.model.staticproperty.OneOfStaticProperty;
import org.apache.streampipes.model.staticproperty.Option;
import org.apache.streampipes.model.staticproperty.PropertyValueSpecification;
import org.apache.streampipes.model.staticproperty.RuntimeResolvableAnyStaticProperty;
import org.apache.streampipes.model.staticproperty.RuntimeResolvableGroupStaticProperty;
import org.apache.streampipes.model.staticproperty.RuntimeResolvableOneOfStaticProperty;
import org.apache.streampipes.model.staticproperty.RuntimeResolvableTreeInputStaticProperty;
import org.apache.streampipes.model.staticproperty.SecretStaticProperty;
import org.apache.streampipes.model.staticproperty.StaticProperty;
import org.apache.streampipes.model.staticproperty.StaticPropertyAlternative;
import org.apache.streampipes.model.staticproperty.StaticPropertyAlternatives;
import org.apache.streampipes.model.staticproperty.StaticPropertyGroup;
import org.apache.streampipes.model.staticproperty.SupportedProperty;
import org.apache.streampipes.sdk.helpers.CodeLanguage;
import org.apache.streampipes.sdk.helpers.Filetypes;
import org.apache.streampipes.sdk.helpers.Label;
import org.apache.streampipes.sdk.helpers.RequirementsSelector;
import org.apache.streampipes.sdk.utils.Datatypes;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StaticProperties {

  public static StaticPropertyAlternatives alternatives(Label label, StaticPropertyAlternative... alternatives) {
    return alternatives(label, Arrays.asList(alternatives));
  }

  public static StaticPropertyAlternatives alternatives(Label label, List<StaticPropertyAlternative> alternatives) {
    StaticPropertyAlternatives alternativesContainer =
        new StaticPropertyAlternatives(label.getInternalId(), label.getLabel(), label.getDescription());

    alternativesContainer.setAlternatives(alternatives);

    return alternativesContainer;
  }

  public static MappingPropertyUnary mappingPropertyUnary(Label label, RequirementsSelector requirementsSelector,
                                                          PropertyScope propertyScope) {
    MappingPropertyUnary mp = new MappingPropertyUnary(label.getInternalId(), label
        .getInternalId(),
        label.getLabel(),
        label.getDescription());

    mp.setRequirementSelector(requirementsSelector.toSelector(label.getInternalId()));
    mp.setPropertyScope(propertyScope.name());

    return mp;
  }

  public static FreeTextStaticProperty stringFreeTextProperty(Label label) {
    return freeTextProperty(label, Datatypes.String);
  }

  public static FreeTextStaticProperty stringFreeTextProperty(Label label, String defaultValue) {
    var property = freeTextProperty(label, Datatypes.String);
    property.setValue(defaultValue);
    return property;
  }

  /**
   * Creates a FreeTextStaticProperty with the specified configuration options allowing only plaintext as input.
   *
   * @param label                    The label for the FreeTextStaticProperty.
   * @param isMultiLine              {@code true} if the FreeTextStaticProperty should support multiline text,
   *                                 {@code false} otherwise.
   * @param arePlaceholdersSupported {@code true} if the FreeTextStaticProperty should support placeholders,
   *                                 {@code false} otherwise.
   * @return A configured FreeTextStaticProperty instance.
   */
  public static FreeTextStaticProperty stringFreeTextProperty(
          Label label,
          boolean isMultiLine,
          boolean arePlaceholdersSupported) {
    var property = stringFreeTextProperty(label);
    property.setMultiLine(isMultiLine);
    property.setPlaceholdersSupported(arePlaceholdersSupported);
    return property;
  }

  public static FreeTextStaticProperty integerFreeTextProperty(Label label) {
    return freeTextProperty(label, Datatypes.Integer);
  }

  public static FreeTextStaticProperty integerFreeTextProperty(Label label, int defaultValue) {
    var property = freeTextProperty(label, Datatypes.Integer);
    property.setValue(String.valueOf(defaultValue));
    return property;
  }


  public static FreeTextStaticProperty doubleFreeTextProperty(Label label) {
    return freeTextProperty(label, Datatypes.Double);
  }

  public static FreeTextStaticProperty freeTextProperty(Label label, Datatypes datatype) {
    FreeTextStaticProperty fsp = new FreeTextStaticProperty(label.getInternalId(), label.getLabel(),
        label.getDescription());
    fsp.setRequiredDatatype(URI.create(datatype.toString()));
    return fsp;
  }

  public static FileStaticProperty fileProperty(Label label) {
    FileStaticProperty fp = new FileStaticProperty(label.getInternalId(), label.getLabel(), label
        .getDescription());

    return fp;
  }

  public static FileStaticProperty fileProperty(Label label, Filetypes... requiredFiletypes) {
    FileStaticProperty fp = fileProperty(label);
    List<String> collectedFiletypes = new ArrayList<>();
    Arrays.stream(requiredFiletypes).forEach(rf -> collectedFiletypes.addAll(rf.getFileExtensions()));
    fp.setRequiredFiletypes(collectedFiletypes);
    return fp;
  }

  public static FileStaticProperty fileProperty(Label label, String... requiredFiletypes) {
    FileStaticProperty fp = fileProperty(label);
    fp.setRequiredFiletypes(Arrays.asList(requiredFiletypes.clone()));
    return fp;
  }

  public static RuntimeResolvableOneOfStaticProperty singleValueSelectionFromContainer(Label label) {
    return new RuntimeResolvableOneOfStaticProperty(label.getInternalId(), label
        .getLabel(), label.getDescription());
  }

  public static RuntimeResolvableOneOfStaticProperty singleValueSelectionFromContainer(Label label,
                                                                                       List<String> dependsOn) {
    RuntimeResolvableOneOfStaticProperty rsp = new RuntimeResolvableOneOfStaticProperty(label.getInternalId(), label
        .getLabel(), label.getDescription());
    rsp.setDependsOn(dependsOn);
    return rsp;
  }

  public static RuntimeResolvableAnyStaticProperty multiValueSelectionFromContainer(Label label) {
    return new RuntimeResolvableAnyStaticProperty(label.getInternalId(), label
        .getLabel(), label.getDescription());
  }

  public static RuntimeResolvableGroupStaticProperty runtimeResolvableGroupStaticProperty(Label label,
                                                                                          List<String> dependsOn) {
    var property = new RuntimeResolvableGroupStaticProperty(label.getInternalId(), label
        .getLabel(), label.getDescription(), dependsOn);
    property.setShowLabel(true);
    return property;
  }

  public static RuntimeResolvableAnyStaticProperty multiValueSelectionFromContainer(Label label,
                                                                                    List<String> dependsOn) {
    RuntimeResolvableAnyStaticProperty rsp =
        new RuntimeResolvableAnyStaticProperty(label.getInternalId(), label
            .getLabel(), label.getDescription());
    rsp.setDependsOn(dependsOn);
    return rsp;
  }


  public static RuntimeResolvableTreeInputStaticProperty runtimeResolvableTreeInput(Label label,
                                                                                    List<String> dependsOn,
                                                                                    boolean resolveDynamically,
                                                                                    boolean multiSelection) {
    RuntimeResolvableTreeInputStaticProperty treeInput = new RuntimeResolvableTreeInputStaticProperty(
        label.getInternalId(),
        label.getLabel(),
        label.getDescription());

    treeInput.setDependsOn(dependsOn);
    treeInput.setResolveDynamically(resolveDynamically);
    treeInput.setMultiSelection(multiSelection);

    return treeInput;
  }

  public static StaticProperty integerFreeTextProperty(Label label,
                                                       PropertyValueSpecification propertyValueSpecification) {
    FreeTextStaticProperty fsp = integerFreeTextProperty(label);
    fsp.setValueSpecification(propertyValueSpecification);
    return fsp;
  }

  public static SupportedProperty supportedDomainProperty(String rdfPropertyUri, boolean required) {
    return new SupportedProperty(rdfPropertyUri, required);
  }

  public static StaticPropertyGroup group(Label label, StaticProperty... sp) {
    List<StaticProperty> staticProperties = Arrays.asList(sp);
    return new StaticPropertyGroup(label.getInternalId(), label.getLabel(),
        label.getDescription(), staticProperties);
  }

  public static StaticPropertyGroup group(Label label, Boolean showLabels, StaticProperty... sp) {
    StaticPropertyGroup group = group(label, sp);
    group.setShowLabel(showLabels);

    return group;
  }

  public static OneOfStaticProperty singleValueSelection(Label label, List<Option> options) {
    OneOfStaticProperty osp = new OneOfStaticProperty(label.getInternalId(), label.getLabel(),
        label.getDescription());
    osp.setOptions(options);

    return osp;
  }

  public static SecretStaticProperty secretValue(Label label) {
    return new SecretStaticProperty(label.getInternalId(),
        label.getLabel(), label.getDescription());
  }

  public static CollectionStaticProperty collection(Label label, boolean horizontalAlignment, StaticProperty... sp) {

    if (sp.length > 1) {
      StaticPropertyGroup group = StaticProperties.group(label);
      group.setHorizontalRendering(horizontalAlignment);
      group.setStaticProperties(Arrays.asList(sp));

      return new CollectionStaticProperty(label.getInternalId(), label.getLabel(),
          label.getDescription(), group);
    } else {
      return new CollectionStaticProperty(label.getInternalId(), label.getLabel(),
          label.getDescription(), sp[0]);
    }
  }

  public static CollectionStaticProperty collection(Label label, StaticProperty... sp) {
    return collection(label, true, sp);
  }

  public static CodeInputStaticProperty codeStaticProperty(Label label,
                                                           CodeLanguage codeLanguage,
                                                           String defaultSkeleton) {
    var codeInputStaticProperty = new CodeInputStaticProperty(label.getInternalId(),
        label.getLabel(), label.getDescription());
    codeInputStaticProperty.setLanguage(codeLanguage.name());
    codeInputStaticProperty.setCodeTemplate(defaultSkeleton);
    return codeInputStaticProperty;
  }
}
