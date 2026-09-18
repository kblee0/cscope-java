package io.cscope.java.dto;

public class FileMetricDto {
    public String fileId;
    public String projectId;
    public String packageName;
    public String fileName;
    public String filePath;
    public int totalLines;
    public int codeLoc;
    public int commentLoc;
    public int classCount;
    public int methodCount;

    public FileMetricDto(String fileId, String projectId) {
        this.fileId = fileId;
        this.projectId = projectId;
    }
}
