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

package org.apache.streampipes.sinks.databases.jvm.milvus;

import com.google.gson.JsonObject;
import io.milvus.param.Constant;
import io.milvus.pool.MilvusClientV2Pool;
import io.milvus.pool.PoolConfig;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.ReleaseCollectionReq;
import io.milvus.v2.service.database.request.CreateDatabaseReq;
import io.milvus.v2.service.vector.request.InsertReq;
import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.extensions.api.pe.context.EventSinkRuntimeContext;
import org.apache.streampipes.model.DataSinkType;
import org.apache.streampipes.model.extensions.ExtensionAssetType;
import org.apache.streampipes.model.graph.DataSinkDescription;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.model.schema.PropertyScope;
import org.apache.streampipes.sdk.builder.DataSinkBuilder;
import org.apache.streampipes.sdk.builder.StreamRequirementsBuilder;
import org.apache.streampipes.sdk.helpers.EpRequirements;
import org.apache.streampipes.sdk.helpers.Labels;
import org.apache.streampipes.sdk.helpers.Locales;
import org.apache.streampipes.wrapper.params.compat.SinkParams;
import org.apache.streampipes.wrapper.standalone.StreamPipesDataSink;

import java.time.Duration;
import java.util.*;

public class MilvusSink extends StreamPipesDataSink{

  private static final String URI_KEY = "milvus_uri";

  private static final String TOKEN_KEY = "milvus_token";

  private static final String DBNAME_KEY = "milvus_dbname";
  private static final String DATABASE_REPLICA_NUMBER_KEY = "database_replica_number";

  private static final String COLLECTION_NAME_KEY = "collection_name";

  private static final String VECTOR_KEY = "vector";

  //private final Gson gson = new Gson();

  private MilvusClientV2Pool pool;
  private MilvusClientV2 client;
  String vector;
  //discussion里介绍一下milvus，贴一个milvus官网链接，pom修改依赖版本
  //数据库，向量配置和表配置移到这里来
  @Override
  public DataSinkDescription declareModel() {
      return DataSinkBuilder
              .create("org.apache.streampipes.sinks.databases.jvm.milvus", 0)
              .withLocales(Locales.EN)
              .withAssets(ExtensionAssetType.DOCUMENTATION, ExtensionAssetType.ICON).
              category(DataSinkType.DATABASE)
              .requiredTextParameter(Labels.withId(URI_KEY))
              .requiredTextParameter(Labels.withId(TOKEN_KEY),"root:Milvus")
              .requiredTextParameter(Labels.withId(DBNAME_KEY))
              .requiredTextParameter(Labels.withId(DATABASE_REPLICA_NUMBER_KEY),"2")
              .requiredTextParameter(Labels.withId(COLLECTION_NAME_KEY))
              .requiredStream(StreamRequirementsBuilder
                      .create()
                      .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                              Labels.withId(VECTOR_KEY),
                              PropertyScope.NONE)
                      .build())
              .build();
  }
  //数据库等的实例化放到初始化这里一起进行
  @Override
  public void onInvocation(SinkParams parameters,
                           EventSinkRuntimeContext runtimeContext) throws SpRuntimeException, ClassNotFoundException, NoSuchMethodException, InterruptedException {
      var extractor = parameters.extractor();
      final String uri = extractor.singleValueParameter(URI_KEY, String.class);
      final String token = extractor.singleValueParameter(TOKEN_KEY, String.class);
      final String dbName = extractor.singleValueParameter(DBNAME_KEY, String.class);

      //create a dataBase
      Map<String, String> properties = new HashMap<>();
      properties.put(Constant.DATABASE_REPLICA_NUMBER,DATABASE_REPLICA_NUMBER_KEY);
      CreateDatabaseReq createDatabaseReq = CreateDatabaseReq.builder()
              .databaseName(DBNAME_KEY)
              .properties(properties)
              .build();
      client.createDatabase(createDatabaseReq);
      client.useDatabase(DBNAME_KEY);

      // create a collection with schema, when indexParams is specified, it will create index as well
      //CreateCollectionReq.CollectionSchema collectionSchema = client.createSchema();
      this.vector = parameters.extractor().mappingPropertyValue(VECTOR_KEY);



      ConnectConfig connectConfig = ConnectConfig.builder()
              .uri(uri)
              .token(token)
              .dbName(dbName)
              .build();

      PoolConfig poolConfig = PoolConfig.builder()
              .maxIdlePerKey(10) // max idle clients per key
              .maxTotalPerKey(20) // max total(idle + active) clients per key
              .maxTotal(100) // max total clients for all keys
              .maxBlockWaitDuration(Duration.ofSeconds(5L)) // getClient() will wait 5 seconds if no idle client available
              .minEvictableIdleDuration(Duration.ofSeconds(10L)) // if number
              .build();

      pool = new MilvusClientV2Pool(poolConfig, connectConfig);
      client = pool.getClient("client_name");
  }

  @Override
  public void onDetach() {
      client.close();
      pool.close();
  }
  //专心做插入操作
  @Override
  public void onEvent(Event event) {
      if(event == null){
          return;
      }

      //List<Float> vectorList = new ArrayList<>();

      JsonObject vector = new JsonObject();
      //从字段集合里取出各个字段，分别与从event中取出的数据配对


      InsertReq insertReq = InsertReq.builder()
              .collectionName(COLLECTION_NAME_KEY)
              .data(Collections.singletonList(vector))
              .build();
      client.insert(insertReq);

      // release collection COLLECTION_NAME_KEY
      ReleaseCollectionReq releaseCollectionReq = ReleaseCollectionReq.builder()
              .collectionName(COLLECTION_NAME_KEY)
              .build();
      client.releaseCollection(releaseCollectionReq);
  }
}
