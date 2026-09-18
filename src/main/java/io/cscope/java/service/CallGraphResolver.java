package io.cscope.java.service;

import io.cscope.java.config.PackageFilter;
import io.cscope.java.dto.CallGraphDto;
import io.cscope.java.dto.MethodMetricDto;
import io.cscope.java.util.SourceIdBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CallGraphResolver {
    public void resolve(List<CallGraphDto> callGraphs, List<MethodMetricDto> internalMethods, PackageFilter filter) {
        Map<String, MethodMetricDto> internalMethodMap = internalMethods.stream()
                .collect(Collectors.toMap(m -> m.methodId, m -> m, (a, b) -> a));

        for (CallGraphDto call : callGraphs) {
            String calleeClassId = SourceIdBuilder.buildClassId("", call.calleeClassName);
            String potentialMethodId = SourceIdBuilder.buildMethodId(calleeClassId, call.calleeMethodName, call.calleeRawSignature);

            if (internalMethodMap.containsKey(potentialMethodId)) {
                call.calleeMethodId = potentialMethodId;
                call.calleeTargetType = "INTERNAL";
            } else if (filter.shouldAnalyze(call.calleeClassName)) {
                call.calleeTargetType = "EXTERNAL_PROJECT";
            } else {
                call.calleeTargetType = "LIBRARY";
            }
        }
    }
}
