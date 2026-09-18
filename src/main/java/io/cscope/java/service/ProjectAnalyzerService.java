package io.cscope.java.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import io.cscope.java.config.AnalyzerConfig;
import io.cscope.java.dto.AnalysisResult;
import io.cscope.java.dto.FileMetric;
import io.cscope.java.dto.ProjectMetric;
import io.cscope.java.parser.ExtendedJdtMetricsVisitor;
import io.cscope.java.parser.JdtParserFactory;
import io.cscope.java.util.FileScanner;
import io.cscope.java.util.LineNumberUtils;
import io.cscope.java.util.SourceIdBuilder;

/** 파싱 → 인메모리 후처리 오케스트레이션. */
public class ProjectAnalyzerService {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AnalysisResult analyze(AnalyzerConfig config) throws IOException {
        List<Path> sourceFiles = FileScanner.findJavaFiles(config.getSourceRoot());
        if (sourceFiles.isEmpty()) {
            throw new IllegalArgumentException("분석할 .java 파일이 없습니다: " + config.getSourceRoot());
        }

        AnalysisResult result = new AnalysisResult();
        result.project = newProjectMetric(config);

        // 1차 파싱: 전체 파일을 한 번에 넘겨야 파일 간 바인딩이 해석된다.
        String[] paths = sourceFiles.stream()
                .map(path -> path.toAbsolutePath().toString())
                .toArray(String[]::new);

        ASTParser parser = JdtParserFactory.newParser(config);
        parser.createASTs(paths, null, new String[0], new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit ast) {
                try {
                    processUnit(config, result, Paths.get(sourceFilePath), ast);
                } catch (IOException e) {
                    throw new UncheckedIOException("소스 파일 읽기 실패: " + sourceFilePath, e);
                }
            }
        }, null);

        // 2차 인메모리 후처리
        CallGraphResolver.resolve(result, config.getPackageFilter());
        MetricAggregator.aggregate(result);
        return result;
    }

    private void processUnit(AnalyzerConfig config, AnalysisResult result, Path file, CompilationUnit unit)
            throws IOException {
        String source = new String(Files.readAllBytes(file), config.getCharset());
        LineNumberUtils.LineCounts counts = LineNumberUtils.count(source);

        String packageName = unit.getPackage() != null
                ? unit.getPackage().getName().getFullyQualifiedName()
                : "";
        String fileName = file.getFileName().toString();

        FileMetric fileMetric = new FileMetric();
        fileMetric.fileId = SourceIdBuilder.fileId(packageName, fileName);
        fileMetric.projectId = config.getProjectId();
        fileMetric.packageName = packageName;
        fileMetric.fileName = fileName;
        fileMetric.filePath = relativePath(config.getSourceRoot(), file);
        fileMetric.totalLines = counts.totalLines;
        fileMetric.codeLoc = counts.codeLoc;
        fileMetric.commentLoc = counts.commentLoc;
        result.files.add(fileMetric);

        unit.accept(new ExtendedJdtMetricsVisitor(unit, fileMetric, config.getPackageFilter(), result));
    }

    private static String relativePath(Path sourceRoot, Path file) {
        return sourceRoot.relativize(file.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }

    private static ProjectMetric newProjectMetric(AnalyzerConfig config) {
        ProjectMetric project = new ProjectMetric();
        project.projectId = config.getProjectId();
        project.projectName = config.getProjectName();
        project.analyzedAt = LocalDateTime.now().format(TIMESTAMP);
        project.packagePath = config.getSourceRoot().toString().replace('\\', '/');
        return project;
    }
}
