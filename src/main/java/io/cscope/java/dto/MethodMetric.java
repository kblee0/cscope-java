package io.cscope.java.dto;

/** method_metric.csv 1 행. */
public class MethodMetric {

    public String methodId;
    public String projectId;
    public String classId;
    public String packageName;
    public String className;
    public String methodName;
    /** 풀 시그니처. call_graph.callee_raw_signature 와 동일 포맷이며 method_id 와 같은 값이다. */
    public String signature;
    public int startLine;
    public int endLine;
    public int loc;
    public int cyclomaticComplexity;
    public int fanIn;
    public int fanOut;
    public int weightedComplexity;
}
