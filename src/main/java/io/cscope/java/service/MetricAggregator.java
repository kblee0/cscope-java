package io.cscope.java.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.cscope.java.dto.AnalysisResult;
import io.cscope.java.dto.ClassMetric;
import io.cscope.java.dto.FileMetric;
import io.cscope.java.dto.MethodMetric;

/**
 * 인메모리 후처리 2단계. class / file / project 레벨 집계.
 */
public final class MetricAggregator {

    private MetricAggregator() {
    }

    public static void aggregate(AnalysisResult result) {
        Map<String, List<MethodMetric>> methodsByClass = new HashMap<>();
        for (MethodMetric method : result.methods) {
            methodsByClass.computeIfAbsent(method.classId, key -> new ArrayList<>()).add(method);
        }

        Map<String, int[]> fileCounters = new HashMap<>(); // [classCount, methodCount]
        for (ClassMetric clazz : result.classes) {
            List<MethodMetric> methods = methodsByClass.getOrDefault(clazz.classId, List.of());
            int total = 0;
            int max = 0;
            for (MethodMetric method : methods) {
                total += method.cyclomaticComplexity;
                max = Math.max(max, method.cyclomaticComplexity);
            }
            clazz.methodCount = methods.size();
            clazz.totalCyclomaticComplexity = total;
            clazz.maxCyclomaticComplexity = max;
            clazz.avgCyclomaticComplexity = methods.isEmpty()
                    ? 0.0
                    : Math.round((total / (double) methods.size()) * 100.0) / 100.0;

            int[] counter = fileCounters.computeIfAbsent(clazz.fileId, key -> new int[2]);
            counter[0]++;
            counter[1] += methods.size();
        }

        int totalLoc = 0;
        for (FileMetric file : result.files) {
            int[] counter = fileCounters.getOrDefault(file.fileId, new int[2]);
            file.classCount = counter[0];
            file.methodCount = counter[1];
            totalLoc += file.codeLoc;
        }

        result.project.totalFiles = result.files.size();
        result.project.totalLoc = totalLoc;
        result.project.totalClasses = result.classes.size();
        result.project.totalMethods = result.methods.size();
    }
}
