package org.apache.streampipes.manager.loadbalance;

import org.apache.streampipes.model.extensions.svcdiscovery.SpServiceRegistration;
import org.apache.streampipes.model.extensions.svcdiscovery.SpServiceStatus;

import java.util.List;

public interface ExtensionServiceSelector {

    SpServiceRegistration select(List<SpServiceRegistration> availableServices, List<String> labels);

}
