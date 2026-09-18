package io.cscope.java.service;

import io.cscope.java.config.AnalyzerConfig;
import io.cscope.java.config.PackageFilter;
import io.cscope.java.dto.*;
import io.cscope.java.parser.ExtendedJdtMetricsVisitor;
import io.cscope.java.parser.JdtParserFactory;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ProjectAnalyzerService {
    private final CallGraphResolver callGraphResolver = new CallGraphResolver();
    private final MetricAggregator metricAggregator = new MetricAggregator();

    public void analyze(AnalyzerConfig config, List<FileMetricDto> fileMetrics, List<ClassMetricDto> classMetrics, List<MethodMetricDto> methodMetrics, List<CallGraphDto> callGraphs) throws IOException {
        PackageFilter filter = new PackageFilter(config.includePackages, config.excludePackages);
        
        List<File> javaFiles = findJavaFiles(new File(config.sourcePath));
        String[] sourcePaths = { config.sourcePath };
        
        // Build Classpath including external JARs (Supports multiple paths separated by ; or :)
        List<String> classpathList = new ArrayList<>();
        if (config.libPath != null) {
            String[] entries = config.libPath.split(File.pathSeparator);
            for (String entry : entries) {
                File file = new File(entry.trim());
                if (file.exists()) {
                    if (file.isDirectory()) {
                        // Add the directory itself (for .class files)
                        classpathList.add(file.getAbsolutePath());
                        // Also add all .jar files inside
                        File[] jars = file.listFiles((dir, name) -> name.endsWith(".jar"));
                        if (jars != null) {
                            for (File jar : jars) classpathList.add(jar.getAbsolutePath());
                        }
                    } else if (file.getName().endsWith(".jar")) {
                        classpathList.add(file.getAbsolutePath());
                    }
                }
            }
        }
        // Include system classpath
        classpathList.add(System.getProperty("java.class.path"));
        String[] classpath = classpathList.toArray(new String[0]);

        ASTParser parser = JdtParserFactory.createParser(classpath, sourcePaths);

        for (File file : javaFiles) {
            String source = Files.readString(file.toPath());
            parser.setSource(source.toCharArray());
            parser.setUnitName(file.getName());
            
            CompilationUnit cu = (CompilationUnit) parser.createAST(null);
            ExtendedJdtMetricsVisitor visitor = new ExtendedJdtMetricsVisitor(config.projectId, file.getAbsolutePath(), source, filter);
            cu.accept(visitor);

            if (visitor.getFileMetric().fileId != null) {
                fileMetrics.add(visitor.getFileMetric());
                classMetrics.addAll(visitor.getClassMetrics());
                methodMetrics.addAll(visitor.getMethodMetrics());
                callGraphs.addAll(visitor.getCallGraphs());
            }
        }

        // Post-processing
        callGraphResolver.resolve(callGraphs, methodMetrics, filter);
        metricAggregator.aggregate(classMetrics, methodMetrics, callGraphs);
    }

    private List<File> findJavaFiles(File dir) {
        List<File> result = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    result.addAll(findJavaFiles(f));
                } else if (f.getName().endsWith(".java")) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    public ProjectDto summarize(AnalyzerConfig config, List<FileMetricDto> fileMetrics, List<ClassMetricDto> classMetrics, List<MethodMetricDto> methodMetrics) {
        ProjectDto project = new ProjectDto(config.projectId, config.projectId, config.sourcePath);
        project.totalFiles = fileMetrics.size();
        project.totalLoc = fileMetrics.stream().mapToInt(f -> f.codeLoc).sum();
        project.totalClasses = classMetrics.size();
        project.totalMethods = methodMetrics.size();
        return project;
    }
}
