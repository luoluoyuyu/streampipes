package org.apache.streampipes.manager.loadbalance.impl;

import org.apache.streampipes.manager.health.ServiceRegistrationManager;
import org.apache.streampipes.manager.loadbalance.*;
import org.apache.streampipes.manager.monitoring.pipeline.ExtensionsLogProvider;
import org.apache.streampipes.model.base.InvocableStreamPipesEntity;
import org.apache.streampipes.model.connect.adapter.AdapterDescription;
import org.apache.streampipes.model.extensions.svcdiscovery.SpServiceRegistration;
import org.apache.streampipes.model.loadbalancer.ResourceUnit;
import org.apache.streampipes.model.loadbalancer.ResourceUnitStats;
import org.apache.streampipes.model.loadbalancer.ServiceUsageReport;
import org.apache.streampipes.model.monitoring.MessageCounter;
import org.apache.streampipes.model.monitoring.SpMetricsEntry;
import org.apache.streampipes.storage.management.StorageDispatcher;

import java.util.*;

public class ExtensibleLoadManager implements LoadBalancer {

    ExtensionServiceSelector selector;


    ServiceRegistrationManager serviceManager;

    LoadData loadData;

    LoadData historicalLoadData;

    PipelineMigrator migrator;

    public ExtensibleLoadManager(ExtensionServiceSelector selector,PipelineMigrator pipelineMigrator) {
        this.selector = selector;
        this.migrator=pipelineMigrator;
        serviceManager = new ServiceRegistrationManager(
                StorageDispatcher.INSTANCE.getNoSqlStore().getExtensionsServiceStorage());
        updateAll();
        historicalLoadData=new LoadData(new HashMap<>(),new HashMap<>());
    }


    @Override
    public SpServiceRegistration allocation(ResourceUnit<InvocableStreamPipesEntity> resourceUnit, List<SpServiceRegistration> serviceRegistrations, List<String> label) {
        SpServiceRegistration spServiceRegistration = selector.select(serviceRegistrations, label);
        PipelineRuntimeData.addSinkAndProcess(resourceUnit, spServiceRegistration);
        return spServiceRegistration;
    }

    public void stopPipeline(String pipId) {
        PipelineRuntimeData.deleteSinkAndProcess(pipId);
    }

    public void stopAdapter(String pipId) {
        PipelineRuntimeData.deleteAdapter(pipId);
    }

    @Override
    public SpServiceRegistration allocationPe(ResourceUnit<AdapterDescription> resourceUnit, List<SpServiceRegistration> serviceRegistrations, List<String> label) {
        SpServiceRegistration spServiceRegistration = selector.select(serviceRegistrations, label);
        PipelineRuntimeData.addAdapter(resourceUnit, spServiceRegistration);
        return spServiceRegistration;
    }


    public void updateAll() {
        historicalLoadData=loadData;
        loadData = new LoadData(getServiceUsage(),getResourceUnitStats());
    }

    private Map<String, ServiceUsageReport> getServiceUsage() {
        Map<String, ServiceUsageReport> map = new HashMap<>();
        ExtensionsLogProvider provider = ExtensionsLogProvider.INSTANCE;
        for (SpServiceRegistration registration : serviceManager.getAivServices()) {
            if (provider.getUsageReports(registration.getSvcId()) != null) {
                map.put(registration.getSvcId(), provider.getUsageReports(registration.getSvcId()));
            }
        }
        return map;
    }

    private Map<String, List<ResourceUnitStats>> getResourceUnitStats() {
        Map<String, List<ResourceUnitStats>> map = new HashMap<>();
        ExtensionsLogProvider provider = ExtensionsLogProvider.INSTANCE;
        for (Map.Entry<String, List<ResourceUnit<InvocableStreamPipesEntity>>> entry : PipelineRuntimeData.getSinksAndProcess().entrySet()) {
            for (ResourceUnit<InvocableStreamPipesEntity> resourceUnit : entry.getValue()) {
                ResourceUnitStats resourceUnitStats = new ResourceUnitStats(resourceUnit.getId());
                long countOut = 0L;
                long countIn = 0;
                long throughputIn = 0;
                long throughputOut = 0;
                for (InvocableStreamPipesEntity entity : resourceUnit.getElements()) {
                    SpMetricsEntry spMetricsEntry = provider.getMetricInfosForResource(entity.getElementId());
                    for (Map.Entry<String, MessageCounter> e : spMetricsEntry.getMessagesIn().entrySet()) {
                        countIn += e.getValue().getCounter();
                        throughputIn += e.getValue().getSize();
                    }
                    countOut += spMetricsEntry.getMessagesOut().getCounter();
                    throughputOut += spMetricsEntry.getMessagesOut().getSize();
                }
                resourceUnitStats.setEventRateIn((double) countIn );
                resourceUnitStats.setEventRateOut((double) countOut );
                resourceUnitStats.setEventThroughputIn((double) throughputIn );
                resourceUnitStats.setEventThroughputOut((double) throughputOut );
                if (!map.containsKey(resourceUnit.getServiceId())) {
                    map.put(resourceUnit.getServiceId(), new ArrayList<>());
                }

                map.get(resourceUnit.getServiceId()).add(resourceUnitStats);

            }
        }

        for (Map.Entry<String, List<ResourceUnit<AdapterDescription>>> entry : PipelineRuntimeData.getAdapter().entrySet()) {
            for (ResourceUnit<AdapterDescription> resourceUnit : entry.getValue()) {
                ResourceUnitStats resourceUnitStats = new ResourceUnitStats(resourceUnit.getId());
                long countOut = 0L;
                long countIn = 0;
                long throughputIn = 0;
                long throughputOut = 0;
                for (AdapterDescription entity : resourceUnit.getElements()) {
                    SpMetricsEntry spMetricsEntry = provider.getMetricInfosForResource(entity.getElementId());
                    for (Map.Entry<String, MessageCounter> e : spMetricsEntry.getMessagesIn().entrySet()) {
                        countIn += e.getValue().getCounter();
                        throughputIn += e.getValue().getSize();
                    }
                    countOut += spMetricsEntry.getMessagesOut().getCounter();
                    throughputOut += spMetricsEntry.getMessagesOut().getSize();
                }
                resourceUnitStats.setEventRateIn((double) countIn );
                resourceUnitStats.setEventRateOut((double) countOut );
                resourceUnitStats.setEventThroughputIn((double) throughputIn );
                resourceUnitStats.setEventThroughputOut((double) throughputOut );
                if (!map.containsKey(resourceUnit.getServiceId())) {
                    map.put(resourceUnit.getServiceId(), new ArrayList<>());
                }

                map.get(resourceUnit.getServiceId()).add(resourceUnitStats);

            }
        }

        return map;
    }

    public void doLoadShedding(){
        migrator.doLoadShedding(serviceManager.getAivServices());
    }

    public LoadData getLoadData() {
        return loadData;
    }

    public LoadData getHistoricalLoadData() {
        return historicalLoadData;
    }
}
