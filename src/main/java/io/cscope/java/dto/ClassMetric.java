package io.cscope.java.dto;

/** class_metric.csv 1 행. */
public class ClassMetric {

    public String classId;
    public String projectId;
    public String fileId;
    public String packageName;
    public String className;
    public String fullClassName;
    public ClassType classType;
    public int startLine;
    public int endLine;
    public int loc;
    public int methodCount;
    public int totalCyclomaticComplexity;
    public int maxCyclomaticComplexity;
    public double avgCyclomaticComplexity;
}
