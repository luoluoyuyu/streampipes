package org.apache.streampipes.manager.loadbalance;


import org.apache.streampipes.model.loadbalancer.ResourceUnitStats;
import org.apache.streampipes.model.loadbalancer.ServiceUsageReport;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoadData {

    private  Map<String, ServiceUsageReport> serviceData;


    private Map<String, List<ResourceUnitStats>> resourceUnitStats;


    public LoadData(Map<String, ServiceUsageReport> serviceData, Map<String, List<ResourceUnitStats>> resourceUnitStats) {
        this.serviceData = serviceData;
        this.resourceUnitStats = resourceUnitStats;
    }


    public ServiceUsageReport getServiceUsage(String id){
        return serviceData.get(id);
    }

    public List<ResourceUnitStats> getResourceUnitStats(String id){
        return resourceUnitStats.get(id);
    }
}
