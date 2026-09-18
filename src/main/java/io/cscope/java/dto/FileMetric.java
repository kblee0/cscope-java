package io.cscope.java.dto;

/** file_metric.csv 1 행. */
public class FileMetric {

    public String fileId;
    public String projectId;
    public String packageName;
    public String fileName;
    /** package_path 기준 상대 경로. */
    public String filePath;
    public int totalLines;
    public int codeLoc;
    public int commentLoc;
    public int classCount;
    public int methodCount;
}
