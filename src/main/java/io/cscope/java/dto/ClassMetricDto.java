package io.cscope.java.dto;

public class ClassMetricDto {
    public String classId;
    public String projectId;
    public String fileId;
    public String packageName;
    public String className;
    public String fullClassName;
    public String classType;
    public int startLine;
    public int endLine;
    public int loc;
    public int methodCount;
    public int totalCyclomaticComplexity;
    public int maxCyclomaticComplexity;
    public double avgCyclomaticComplexity;

    public ClassMetricDto(String classId, String projectId, String fileId) {
        this.classId = classId;
        this.projectId = projectId;
        this.fileId = fileId;
    }
}
