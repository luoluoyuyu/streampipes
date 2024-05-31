package org.apache.streampipes.manager.loadbalance.impl;

import org.apache.streampipes.manager.loadbalance.ExtensionServiceSelector;
import org.apache.streampipes.manager.loadbalance.LoadData;
import org.apache.streampipes.manager.loadbalance.LoadManager;
import org.apache.streampipes.manager.loadbalance.ServiceLoadCalculator;
import org.apache.streampipes.manager.monitoring.pipeline.ExtensionsLogProvider;
import org.apache.streampipes.model.extensions.svcdiscovery.SpServiceRegistration;
import org.apache.streampipes.model.loadbalancer.ServiceUsageReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinimumLoadSelector implements ExtensionServiceSelector {


    @Override
    public SpServiceRegistration select(List<SpServiceRegistration> availableServices, List<String> labels) {
        LoadManager.updateAll();
        List<SpServiceRegistration> serviceRegistrations =new ArrayList<>(availableServices);
        serviceRegistrations.sort((a,b)->{
            float fa=ServiceLoadCalculator.calculateLoad(a.getSvcId());

            float fb =ServiceLoadCalculator.calculateLoad(b.getSvcId());

            return Float.compare(fa,fb);
        });

        return serviceRegistrations.get(0);
    }
}
