package io.cscope.java.service;

import io.cscope.java.dto.CallGraphDto;
import io.cscope.java.dto.ClassMetricDto;
import io.cscope.java.dto.MethodMetricDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetricAggregator {
    public void aggregate(List<ClassMetricDto> classes, List<MethodMetricDto> methods, List<CallGraphDto> calls) {
        // Calculate Fan-out
        Map<String, Long> fanOutMap = calls.stream()
                .collect(Collectors.groupingBy(c -> c.callerMethodId, Collectors.counting()));
        
        // Calculate Fan-in
        Map<String, Long> fanInMap = calls.stream()
                .filter(c -> c.calleeMethodId != null)
                .collect(Collectors.groupingBy(c -> c.calleeMethodId, Collectors.counting()));

        // Update Method Metrics
        for (MethodMetricDto m : methods) {
            m.fanOut = fanOutMap.getOrDefault(m.methodId, 0L).intValue();
            m.fanIn = fanInMap.getOrDefault(m.methodId, 0L).intValue();
            m.weightedComplexity = (int) Math.round(m.cyclomaticComplexity + (m.fanIn * 1.5) + (m.fanOut * 1.0));
        }

        // Aggregate Class Metrics
        Map<String, List<MethodMetricDto>> classToMethods = methods.stream()
                .collect(Collectors.groupingBy(m -> m.classId));

        for (ClassMetricDto c : classes) {
            List<MethodMetricDto> classMethods = classToMethods.get(c.classId);
            if (classMethods != null && !classMethods.isEmpty()) {
                c.totalCyclomaticComplexity = classMethods.stream().mapToInt(m -> m.cyclomaticComplexity).sum();
                c.maxCyclomaticComplexity = classMethods.stream().mapToInt(m -> m.cyclomaticComplexity).max().orElse(0);
                c.avgCyclomaticComplexity = classMethods.stream().mapToInt(m -> m.cyclomaticComplexity).average().orElse(0.0);
            }
        }
    }
}
