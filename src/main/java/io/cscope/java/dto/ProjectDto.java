package io.cscope.java.dto;

import java.time.LocalDateTime;

public class ProjectDto {
    public String projectId;
    public String projectName;
    public LocalDateTime analyzedAt;
    public String packagePath;
    public int totalFiles;
    public int totalLoc;
    public int totalClasses;
    public int totalMethods;

    public ProjectDto(String projectId, String projectName, String packagePath) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.packagePath = packagePath;
        this.analyzedAt = LocalDateTime.now();
    }
}
