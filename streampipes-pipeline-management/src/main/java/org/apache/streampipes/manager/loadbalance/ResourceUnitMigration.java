package org.apache.streampipes.manager.loadbalance;

import org.apache.http.entity.InputStreamEntity;
import org.apache.streampipes.commons.exceptions.NoServiceEndpointsAvailableException;
import org.apache.streampipes.manager.execution.endpoint.ExtensionsServiceEndpointUtils;
import org.apache.streampipes.manager.execution.http.DetachHttpRequest;
import org.apache.streampipes.manager.execution.http.InvokeHttpRequest;
import org.apache.streampipes.manager.monitoring.pipeline.ExtensionsLogProvider;
import org.apache.streampipes.manager.storage.RunningPipelineElementStorage;
import org.apache.streampipes.model.base.InvocableStreamPipesEntity;
import org.apache.streampipes.model.connect.adapter.AdapterDescription;
import org.apache.streampipes.model.extensions.svcdiscovery.SpServiceRegistration;
import org.apache.streampipes.model.graph.DataProcessorInvocation;
import org.apache.streampipes.model.graph.DataSinkInvocation;
import org.apache.streampipes.model.loadbalancer.ResourceUnit;
import org.apache.streampipes.model.loadbalancer.ResourceUnitStats;
import org.apache.streampipes.model.pipeline.Pipeline;
import org.apache.streampipes.storage.management.StorageDispatcher;

import java.util.ArrayList;
import java.util.List;

public class ResourceUnitMigration {

    public static void migration(ResourceUnit<InvocableStreamPipesEntity> resourceUnit, SpServiceRegistration registration) {
        resourceUnit.setServiceId(registration.getSvcId());
        for (InvocableStreamPipesEntity pipesEntity : resourceUnit.getElements()) {
            String endpointUrl = pipesEntity.getSelectedEndpointUrl()+ pipesEntity.getDetachPath();
            new DetachHttpRequest().execute(pipesEntity, endpointUrl, resourceUnit.getPipelineId());
        }

        for (InvocableStreamPipesEntity pipesEntity : resourceUnit.getElements()) {
            pipesEntity.setSelectedEndpointUrl(getSelectedEndpoint(pipesEntity, registration.getServiceUrl()));
            String endpointUrl = pipesEntity.getSelectedEndpointUrl();
            new InvokeHttpRequest().execute(pipesEntity, endpointUrl, resourceUnit.getPipelineId());
        }

        Pipeline pipeline = StorageDispatcher.INSTANCE.getNoSqlStore().getPipelineStorageAPI().getPipeline(resourceUnit.getPipelineId());

        for (DataSinkInvocation dataSinkInvocation : pipeline.getActions()) {
            for (InvocableStreamPipesEntity entity : resourceUnit.getElements()) {
                if (dataSinkInvocation.getElementId().equals(entity.getElementId())) {
                    dataSinkInvocation.setSelectedEndpointUrl(entity.getSelectedEndpointUrl());
                }
            }
        }
        for (DataProcessorInvocation processorInvocation : pipeline.getSepas()) {
            for (InvocableStreamPipesEntity entity : resourceUnit.getElements()) {
                if (processorInvocation.getElementId().equals(entity.getElementId())) {
                    processorInvocation.setSelectedEndpointUrl(entity.getSelectedEndpointUrl());
                }
            }
        }
        StorageDispatcher.INSTANCE.getNoSqlStore().getPipelineStorageAPI().updatePipeline(pipeline);
    }

    public static void migrationForHealth(ResourceUnit<InvocableStreamPipesEntity> resourceUnit, SpServiceRegistration registration) {
        System.out.println("update resourceUnit to:"+registration.getHost());
        resourceUnit.setServiceId(registration.getSvcId());
        try {
            for (InvocableStreamPipesEntity pipesEntity : resourceUnit.getElements()) {
                pipesEntity.setSelectedEndpointUrl(getSelectedEndpoint(pipesEntity, registration.getServiceUrl()));
                String endpointUrl = pipesEntity.getSelectedEndpointUrl();
                new InvokeHttpRequest().execute(pipesEntity, endpointUrl, resourceUnit.getPipelineId());
            }

            Pipeline pipeline = StorageDispatcher.INSTANCE.getNoSqlStore().getPipelineStorageAPI().getPipeline(resourceUnit.getPipelineId());

            for (DataProcessorInvocation processorInvocation : pipeline.getSepas()) {
                for (InvocableStreamPipesEntity entity : resourceUnit.getElements()) {
                    if (processorInvocation.getElementId().equals(entity.getElementId())) {
                        processorInvocation.setSelectedEndpointUrl(entity.getSelectedEndpointUrl());
                    }
                }
            }

            for (DataSinkInvocation dataSinkInvocation : pipeline.getActions()) {
                for (InvocableStreamPipesEntity entity : resourceUnit.getElements()) {
                    if (dataSinkInvocation.getElementId().equals(entity.getElementId())) {
                        dataSinkInvocation.setSelectedEndpointUrl(entity.getSelectedEndpointUrl());
                    }
                }
            }
            StorageDispatcher.INSTANCE.getNoSqlStore().getPipelineStorageAPI().updatePipeline(pipeline);
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    public static void migration(SpServiceRegistration sourceService, double source, SpServiceRegistration targetService,double target) {
        List<ResourceUnit<InvocableStreamPipesEntity>> units = PipelineRuntimeData.getServiceResourceUnit(sourceService.getSvcId());
        LoadData loadData = LoadManager.getLoadData();
        List<ResourceUnitStats> list = loadData.getResourceUnitStats(sourceService.getSvcId());
        if(list==null){
            list=new ArrayList<>();
        }
        list.sort((a, b) -> {
            return Double.compare(  b.eventRateOut, a.eventRateOut);
        });

        List<ResourceUnitStats> list1 = loadData.getResourceUnitStats(targetService.getSvcId());
        if(list1==null){
            list1=new ArrayList<>();
        }
        double all =0;
        for(ResourceUnitStats stats:list){
            all +=  stats.eventRateOut;
        }
        double num=0;
        for(ResourceUnitStats stats:list1){
            num+=  stats.eventRateOut;

        }
        all += num;

        double tar=0;
        for(ResourceUnitStats resourceUnitStats : list){
            if(tar>=(((all-num)*(source-target)/(2*source)))){
                System.out.println("target:out "+tar);
                System.out.println("target:neet Tran:" + (((all-num)*(source-target)/(2*source))));
                break;
            }

            for(ResourceUnit<InvocableStreamPipesEntity> unit : units){
                if(unit.getId().equals(resourceUnitStats.getResourceId())){
                    try {
                        migration(unit,targetService);
                        tar+= resourceUnitStats.eventRateOut;
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    private static String getSelectedEndpoint(InvocableStreamPipesEntity pipelineElement, String url) {
        return ExtensionsServiceEndpointUtils
                .getPipelineElementType(pipelineElement)
                .getInvocationUrl(url, pipelineElement.getAppId());
    }
}
