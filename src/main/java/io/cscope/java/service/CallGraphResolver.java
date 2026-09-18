package io.cscope.java.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.cscope.java.config.PackageFilter;
import io.cscope.java.dto.AnalysisResult;
import io.cscope.java.dto.CalleeTargetType;
import io.cscope.java.dto.CallEdge;
import io.cscope.java.dto.ClassMetric;
import io.cscope.java.dto.MethodMetric;

/**
 * 인메모리 후처리 1단계.
 *
 * <ul>
 *   <li>callee_raw_signature ↔ method_metric.signature 매칭으로 callee_method_id 확정</li>
 *   <li>callee_target_type(INTERNAL / EXTERNAL_PROJECT / LIBRARY) 확정</li>
 *   <li>fan_in / fan_out / weighted_complexity 산출</li>
 * </ul>
 */
public final class CallGraphResolver {

    private CallGraphResolver() {
    }

    public static void resolve(AnalysisResult result, PackageFilter filter) {
        Map<String, MethodMetric> methodBySignature = new HashMap<>();
        for (MethodMetric method : result.methods) {
            methodBySignature.putIfAbsent(method.signature, method);
        }
        Set<String> projectClasses = new HashSet<>();
        for (ClassMetric clazz : result.classes) {
            projectClasses.add(clazz.classId);
        }

        Map<String, Integer> fanIn = new HashMap<>();
        Map<String, Integer> fanOut = new HashMap<>();

        for (CallEdge edge : result.calls) {
            MethodMetric callee = methodBySignature.get(edge.calleeRawSignature);
            if (callee != null) {
                edge.calleeMethodId = callee.methodId;
                edge.calleeTargetType = CalleeTargetType.INTERNAL;
                fanIn.merge(callee.methodId, 1, Integer::sum);
            } else if (edge.calleeFromSource
                    || projectClasses.contains(edge.calleeClassName)
                    || filter.matchesInclude(edge.calleeClassName)) {
                // 소스 기반 타입이지만 이번 분석 범위에서 메서드를 특정하지 못한 경우
                edge.calleeTargetType = CalleeTargetType.EXTERNAL_PROJECT;
            } else {
                edge.calleeTargetType = CalleeTargetType.LIBRARY;
            }
            fanOut.merge(edge.callerMethodId, 1, Integer::sum);
        }

        for (MethodMetric method : result.methods) {
            method.fanIn = fanIn.getOrDefault(method.methodId, 0);
            method.fanOut = fanOut.getOrDefault(method.methodId, 0);
            double weighted = method.cyclomaticComplexity + (method.fanIn * 1.5) + (method.fanOut * 1.0);
            method.weightedComplexity = (int) Math.round(weighted);
        }
    }
}
