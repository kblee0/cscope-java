package io.cscope.java.dto;

/** project.csv 1 행. */
public class ProjectMetric {

    public String projectId;
    public String projectName;
    /** yyyy-MM-dd HH:mm:ss */
    public String analyzedAt;
    /** 분석 기준 소스 루트(절대 경로). file_path 의 기준 경로. */
    public String packagePath;
    public int totalFiles;
    public int totalLoc;
    public int totalClasses;
    public int totalMethods;
}
