package io.cscope.java;

import io.cscope.java.cli.CliOptions;
import io.cscope.java.cli.CliOptionsParser;
import io.cscope.java.config.AnalyzerConfig;
import io.cscope.java.dto.*;
import io.cscope.java.export.CsvExportService;
import io.cscope.java.service.ProjectAnalyzerService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JdtProjectAnalyzerMain {
    public static void main(String[] args) {
        CliOptionsParser cliParser = new CliOptionsParser();
        CliOptions options = cliParser.parse(args);

        if (options.projectId == null || options.sourcePath == null) {
            options.printHelp();
            return;
        }

        File outputDir = new File(options.outputPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        AnalyzerConfig config = new AnalyzerConfig(options.projectId, options.sourcePath, options.outputPath);
        config.libPath = options.libPath;
        config.includePackages = options.includePackages;
        config.excludePackages = options.excludePackages;

        ProjectAnalyzerService analyzerService = new ProjectAnalyzerService();
        CsvExportService exportService = new CsvExportService();

        List<FileMetricDto> fileMetrics = new ArrayList<>();
        List<ClassMetricDto> classMetrics = new ArrayList<>();
        List<MethodMetricDto> methodMetrics = new ArrayList<>();
        List<CallGraphDto> callGraphs = new ArrayList<>();

        try {
            System.out.println("Starting analysis for project: " + options.projectId);
            analyzerService.analyze(config, fileMetrics, classMetrics, methodMetrics, callGraphs);
            
            ProjectDto projectSummary = analyzerService.summarize(config, fileMetrics, classMetrics, methodMetrics);
            
            System.out.println("Exporting results to: " + options.outputPath);
            exportService.export(options.outputPath, projectSummary, fileMetrics, classMetrics, methodMetrics, callGraphs);
            
            System.out.println("Analysis completed successfully.");
            System.out.println("Total Files: " + projectSummary.totalFiles);
            System.out.println("Total LoC: " + projectSummary.totalLoc);
            System.out.println("Total Classes: " + projectSummary.totalClasses);
            System.out.println("Total Methods: " + projectSummary.totalMethods);
            
        } catch (Exception e) {
            System.err.println("Error during analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
