package io.cscope.java;

import io.cscope.java.cli.CliOptions;
import io.cscope.java.cli.CliOptionsParser;
import io.cscope.java.config.AnalyzerConfig;
import io.cscope.java.dto.AnalysisResult;
import io.cscope.java.export.CsvExportService;
import io.cscope.java.service.ProjectAnalyzerService;

/** CLI 진입점. */
public final class JdtProjectAnalyzerMain {

    public static void main(String[] args) {
        try {
            CliOptions options = CliOptionsParser.parse(args);
            if (options.help) {
                System.out.println(CliOptionsParser.USAGE);
                return;
            }

            AnalyzerConfig config = AnalyzerConfig.from(options);
            long startedAt = System.currentTimeMillis();

            AnalysisResult result = new ProjectAnalyzerService().analyze(config);
            new CsvExportService().export(result, config.getOutputDir());

            System.out.printf("[cscope-java] project=%s files=%d classes=%d methods=%d calls=%d (%,d ms)%n",
                    result.project.projectId,
                    result.project.totalFiles,
                    result.project.totalClasses,
                    result.project.totalMethods,
                    result.calls.size(),
                    System.currentTimeMillis() - startedAt);
            System.out.println("[cscope-java] output: " + config.getOutputDir());
        } catch (IllegalArgumentException e) {
            System.err.println("[cscope-java] " + e.getMessage());
            System.err.println();
            System.err.println(CliOptionsParser.USAGE);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("[cscope-java] 분석 중 오류가 발생했습니다: " + e);
            e.printStackTrace();
            System.exit(2);
        }
    }

    private JdtProjectAnalyzerMain() {
    }
}
