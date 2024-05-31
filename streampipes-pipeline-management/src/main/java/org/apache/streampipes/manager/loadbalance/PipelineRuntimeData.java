package org.apache.streampipes.manager.loadbalance;

import org.apache.streampipes.extensions.api.connect.IAdapterConfiguration;
import org.apache.streampipes.model.base.InvocableStreamPipesEntity;
import org.apache.streampipes.model.connect.adapter.AdapterDescription;
import org.apache.streampipes.model.extensions.svcdiscovery.SpServiceRegistration;
import org.apache.streampipes.model.loadbalancer.ResourceUnit;
import org.apache.streampipes.storage.api.IAdapterStorage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PipelineRuntimeData {

    static Map<String, List<ResourceUnit<InvocableStreamPipesEntity>>> sinksAndProcess;

    static Map<String, List<ResourceUnit<AdapterDescription>>> adapter;


    static  {
        sinksAndProcess = new ConcurrentHashMap<>();
        adapter = new ConcurrentHashMap<>();
    }

    public  static void addSinkAndProcess(ResourceUnit<InvocableStreamPipesEntity> unit, SpServiceRegistration registration) {
        if (sinksAndProcess.containsKey(unit.getPipelineId())) {
            List<ResourceUnit<InvocableStreamPipesEntity>> entities = sinksAndProcess.get(unit.getPipelineId());
            for (int i=0;i<sinksAndProcess.get(unit.getPipelineId()).size();){
                if(entities.get(i).getId().equals(unit.getId())){
                    entities.remove(i);
                    break;
                }else {
                    i++;
                }
            }
            sinksAndProcess.get(unit.getPipelineId()).add(unit);
        } else {
            sinksAndProcess.put(unit.getPipelineId(), new ArrayList<>());
            sinksAndProcess.get(unit.getPipelineId()).add(unit);
        }
    }

    public  static void deleteSinkAndProcess(String pipId) {
        sinksAndProcess.remove(pipId);
    }

    public  static void deleteAdapter(String adapterId) {
        adapter.remove(adapterId);
    }

    public  static void addAdapter(ResourceUnit<AdapterDescription> unit, SpServiceRegistration registration) {
        if (adapter.containsKey(unit.getPipelineId())) {
            List<ResourceUnit<AdapterDescription>> entities = adapter.get(registration.getSvcId());
            for (int i=0;i<adapter.get(unit.getPipelineId()).size();){
                if(entities.get(i).getPipelineId().equals(unit.getPipelineId())){
                    entities.remove(i);
                    break;
                }else {
                    i++;
                }
            }
            adapter.get(unit.getPipelineId()).add(unit);
        } else {
            adapter.put(unit.getPipelineId(), new ArrayList<>());
            adapter.get(unit.getPipelineId()).add(unit);
        }
    }

    public  static Map<String, List<ResourceUnit<InvocableStreamPipesEntity>>> getSinksAndProcess() {
        return sinksAndProcess;
    }

    public  static Map<String, List<ResourceUnit<AdapterDescription>>> getAdapter() {
        return adapter;
    }

    public   static List<ResourceUnit<InvocableStreamPipesEntity>> getServiceResourceUnit(String serviceId){
        List<ResourceUnit<InvocableStreamPipesEntity>> resourceUnits = new ArrayList<>();
        for(Map.Entry<String,List<ResourceUnit<InvocableStreamPipesEntity>>> entry : sinksAndProcess.entrySet()){
            for(ResourceUnit<InvocableStreamPipesEntity> resourceUnit : entry.getValue()){
                if(resourceUnit.getServiceId().equals(serviceId)) {
                    resourceUnits.add(resourceUnit);
                }
            }
        }
        return resourceUnits;
    }

    public   static void removeServiceResourceUnit(String serviceId){
        for(Map.Entry<String,List<ResourceUnit<InvocableStreamPipesEntity>>> entry : sinksAndProcess.entrySet()){
            List<ResourceUnit<InvocableStreamPipesEntity>> list = entry.getValue();
            for(int i=0;i<list.size();){
                if(list.get(i).getServiceId().equals(serviceId)){
                    list.remove(i);
                }else {
                    i++;
                }
            }
        }

        for(Map.Entry<String,List<ResourceUnit<AdapterDescription>>> entry : adapter.entrySet()){
            List<ResourceUnit<AdapterDescription>> list = entry.getValue();
            for(int i=0;i<list.size();){
                if(list.get(i).getServiceId().equals(serviceId)){
                    list.remove(i);
                }else {
                    i++;
                }
            }
        }
    }

    public   static List<ResourceUnit<AdapterDescription>> getServiceAdapter(String serviceId){
        List<ResourceUnit<AdapterDescription>> resourceUnits = new ArrayList<>();
        for(Map.Entry<String,List<ResourceUnit<AdapterDescription>>> entry : adapter.entrySet()){
            for(ResourceUnit<AdapterDescription> resourceUnit : entry.getValue()){
                if(resourceUnit.getServiceId().equals(serviceId)) {
                    resourceUnits.add(resourceUnit);
                }
            }
        }
        return resourceUnits;
    }


}
