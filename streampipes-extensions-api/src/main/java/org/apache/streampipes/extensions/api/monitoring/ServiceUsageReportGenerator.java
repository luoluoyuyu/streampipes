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
package org.apache.streampipes.extensions.api.monitoring;

import com.sun.management.OperatingSystemMXBean;
import org.apache.streampipes.model.loadbalancer.ServiceUsageReport;
import org.apache.streampipes.model.loadbalancer.Usage;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServiceUsageReportGenerator {

    // The interval for host usage check command
    private static final int CPU_CHECK_MILLIS = 100;
    private static double totalCpuLimit;
    private static double cpuUsageSum = 0d;
    private static int cpuUsageCount = 0;
    private static OperatingSystemMXBean systemBean;
    private static ServiceUsageReport usage;

    private static ScheduledExecutorService executorService;

    static {

        executorService = Executors.newSingleThreadScheduledExecutor();
        systemBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        usage = new ServiceUsageReport();
        totalCpuLimit = getTotalCpuLimit();
        // Call now to initialize values before the constructor returns
        calculateUsage();
        executorService.scheduleWithFixedDelay(ServiceUsageReportGenerator::checkCpuLoad,
                CPU_CHECK_MILLIS,
                CPU_CHECK_MILLIS, TimeUnit.MILLISECONDS);
        executorService.scheduleWithFixedDelay(ServiceUsageReportGenerator::doCalculateUsage,
                1,
                1, TimeUnit.MINUTES);

    }


    public static ServiceUsageReport generateReport() {
        return usage;
    }

    private static synchronized void checkCpuLoad() {
        cpuUsageSum += systemBean.getCpuLoad();
        cpuUsageCount++;
    }


    public static void calculateUsage() {
        checkCpuLoad();
        doCalculateUsage();
    }

    static void doCalculateUsage() {
        ServiceUsageReport usage = new ServiceUsageReport();
        usage.setCpu(getCpuUsage());
        usage.setMemory(getMemUsage());

        ServiceUsageReportGenerator.usage = usage;
    }

    private static double getTotalCpuLimit() {
        return 100 * Runtime.getRuntime().availableProcessors();
    }

    private static synchronized double getTotalCpuUsage() {
        if (cpuUsageCount == 0) {
            return 0;
        }
        double cpuUsage = cpuUsageSum / cpuUsageCount;
        cpuUsageSum = 0d;
        cpuUsageCount = 0;
        return cpuUsage;
    }

    private static Usage getCpuUsage() {
        return new Usage(getTotalCpuUsage() * totalCpuLimit, totalCpuLimit);
    }

    private static Usage getMemUsage() {
        double total = ((double) systemBean.getTotalMemorySize()) / (1024 * 1024);
        double free = ((double) systemBean.getFreeMemorySize()) / (1024 * 1024);
        return new Usage(total - free, total);
    }

//    private static Usage getDirMemUsage() {
//        return new Usage((double) (getJvmDirectMemoryUsed() / MIBI),
//                (double) (DirectMemoryUtils.jvmMaxDirectMemory() / MIBI))
//    }




}
