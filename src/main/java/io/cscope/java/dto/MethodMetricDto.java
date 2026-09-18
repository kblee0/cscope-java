package io.cscope.java.dto;

public class MethodMetricDto {
    public String methodId;
    public String projectId;
    public String classId;
    public String packageName;
    public String className;
    public String methodName;
    public String signature;
    public int startLine;
    public int endLine;
    public int loc;
    public int cyclomaticComplexity;
    public int fanIn;
    public int fanOut;
    public int weightedComplexity;

    public MethodMetricDto(String methodId, String projectId, String classId) {
        this.methodId = methodId;
        this.projectId = projectId;
        this.classId = classId;
    }
}
