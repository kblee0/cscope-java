package io.cscope.java.config;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.cscope.java.cli.CliOptions;
import io.cscope.java.util.FileScanner;

/** 분석 1회 실행에 필요한 확정 설정값. */
public class AnalyzerConfig {

    private final String projectId;
    private final String projectName;
    private final Path sourceRoot;
    private final Path outputDir;
    private final String[] classpathEntries;
    private final String[] sourcepathEntries;
    private final Charset charset;
    private final PackageFilter packageFilter;

    private AnalyzerConfig(String projectId, String projectName, Path sourceRoot, Path outputDir,
            String[] classpathEntries, String[] sourcepathEntries, Charset charset, PackageFilter packageFilter) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.sourceRoot = sourceRoot;
        this.outputDir = outputDir;
        this.classpathEntries = classpathEntries;
        this.sourcepathEntries = sourcepathEntries;
        this.charset = charset;
        this.packageFilter = packageFilter;
    }

    public static AnalyzerConfig from(CliOptions options) throws IOException {
        Path sourceRoot = Paths.get(options.sourcePath).toAbsolutePath().normalize();
        if (!Files.isDirectory(sourceRoot)) {
            throw new IllegalArgumentException("소스 경로가 디렉터리가 아닙니다: " + sourceRoot);
        }
        Path outputDir = Paths.get(options.outputPath).toAbsolutePath().normalize();

        List<Path> jars = List.of();
        if (options.libPath != null) {
            Path libPath = Paths.get(options.libPath).toAbsolutePath().normalize();
            if (!Files.exists(libPath)) {
                throw new IllegalArgumentException("라이브러리 경로가 존재하지 않습니다: " + libPath);
            }
            jars = FileScanner.findJarFiles(libPath);
        }
        String[] classpathEntries = jars.stream().map(Path::toString).toArray(String[]::new);
        String[] sourcepathEntries = { sourceRoot.toString() };

        PackageFilter filter = new PackageFilter(options.includePrefixes, options.excludePrefixes);
        return new AnalyzerConfig(options.projectId, options.projectId, sourceRoot, outputDir,
                classpathEntries, sourcepathEntries, StandardCharsets.UTF_8, filter);
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public Path getSourceRoot() {
        return sourceRoot;
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public String[] getClasspathEntries() {
        return classpathEntries;
    }

    public String[] getSourcepathEntries() {
        return sourcepathEntries;
    }

    public Charset getCharset() {
        return charset;
    }

    public PackageFilter getPackageFilter() {
        return packageFilter;
    }
}
