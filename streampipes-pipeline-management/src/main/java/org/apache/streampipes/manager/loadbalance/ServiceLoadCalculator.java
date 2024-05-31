package org.apache.streampipes.manager.loadbalance;

import org.apache.streampipes.commons.prometheus.service.ElementServiceStats;
import org.apache.streampipes.model.loadbalancer.ServiceUsageReport;

import java.util.List;

public class ServiceLoadCalculator {

    private static float calculate(float historicalLoad,float load, float historicalLoadFactor,ElementServiceStats stats){
        stats.systemLoad=load;
        stats.historicalSystemLoad=historicalLoad;
        stats.currentSystemLoad=(1-historicalLoadFactor)*load + (historicalLoadFactor)*historicalLoad;
        System.out.println("load："+load+" hisLoad："+historicalLoad+" factor： "+ historicalLoadFactor+" return:"+stats.currentSystemLoad);
        return (float) stats.currentSystemLoad;
    }

    private static float calculate(ServiceUsageReport serviceUsageReport,ElementServiceStats stats){
        if(serviceUsageReport ==null){
            return 0.0F;
        }
        stats.cpuUsage=serviceUsageReport.getCpu().percentUsage()*LoadBalancerConfig.CPUResourceWeigh;
        stats.memoryUsage=serviceUsageReport.getMemory().percentUsage()*LoadBalancerConfig.MemoryResourceWeight;
        return (float) Math.max(
                stats.cpuUsage,
                stats.memoryUsage);
    }

    public static float calculateAVG(List<Float> list ){
        float f= 0.0F;
        for(float d :list){
            f+=d;
        }
        f=f/list.size();
        return f;
    }


    public static float calculateLoad(String serviceId){
        LoadData loadData = LoadManager.getLoadData();
        LoadData historicalLoadData = LoadManager.getHistoricalLoadData();
        ElementServiceStats stats = ElementServiceStats.elementServiceStats.get(serviceId);
        if(stats==null){
            stats=new ElementServiceStats(serviceId);
        }
        return ServiceLoadCalculator.calculate(
                ServiceLoadCalculator.calculate(historicalLoadData.getServiceUsage(serviceId),stats),
                ServiceLoadCalculator.calculate(loadData.getServiceUsage(serviceId),stats)
                ,LoadBalancerConfig.HistoryResourcePercentage,stats);

    }
}
