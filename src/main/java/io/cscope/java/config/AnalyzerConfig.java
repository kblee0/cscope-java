package io.cscope.java.config;

import java.util.List;

public class AnalyzerConfig {
    public String projectId;
    public String sourcePath;
    public String libPath;
    public String outputPath;
    public List<String> includePackages;
    public List<String> excludePackages;

    public AnalyzerConfig(String projectId, String sourcePath, String outputPath) {
        this.projectId = projectId;
        this.sourcePath = sourcePath;
        this.outputPath = outputPath;
    }
}
