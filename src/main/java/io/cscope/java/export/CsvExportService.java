package io.cscope.java.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import io.cscope.java.dto.AnalysisResult;

/**
 * 5개 테이블과 1:1 대응하는 CSV 출력.
 *
 * <p>구분자 콤마, 헤더 포함, UTF-8, LF 개행. 콤마/개행/따옴표가 포함된 값만 큰따옴표로 감싼다.
 * null 은 따옴표 없는 빈 값으로 기록되어 PostgreSQL COPY 에서 NULL 로 인식된다.
 */
public class CsvExportService {

    public void export(AnalysisResult result, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);

        write(outputDir.resolve("project.csv"), CsvRowMapper.PROJECT_HEADER,
                List.<Object[]>of(CsvRowMapper.toRow(result.project)));
        write(outputDir.resolve("file_metric.csv"), CsvRowMapper.FILE_HEADER,
                rows(result.files, CsvRowMapper::toRow));
        write(outputDir.resolve("class_metric.csv"), CsvRowMapper.CLASS_HEADER,
                rows(result.classes, CsvRowMapper::toRow));
        write(outputDir.resolve("method_metric.csv"), CsvRowMapper.METHOD_HEADER,
                rows(result.methods, CsvRowMapper::toRow));
        write(outputDir.resolve("call_graph.csv"), CsvRowMapper.CALL_GRAPH_HEADER,
                rows(result.calls, CsvRowMapper::toRow));
    }

    private static <T> List<Object[]> rows(List<T> source, Function<T, Object[]> mapper) {
        List<Object[]> rows = new ArrayList<>(source.size());
        for (T item : source) {
            rows.add(mapper.apply(item));
        }
        return rows;
    }

    private void write(Path file, String[] header, List<Object[]> rows) throws IOException {
        CSVFormat format = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader(header)
                .setRecordSeparator("\n")
                .build();
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (Object[] row : rows) {
                printer.printRecord(row);
            }
        }
    }
}
