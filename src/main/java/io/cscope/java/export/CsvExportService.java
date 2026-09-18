package io.cscope.java.export;

import io.cscope.java.dto.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvExportService {
    public void export(String outputPath, ProjectDto project, List<FileMetricDto> files, List<ClassMetricDto> classes, List<MethodMetricDto> methods, List<CallGraphDto> calls) throws IOException {
        writeCsv(outputPath + "/project.csv", CsvRowMapper.PROJECT_HEADERS, List.of(project), CsvRowMapper::mapProject);
        writeCsv(outputPath + "/file_metric.csv", CsvRowMapper.FILE_HEADERS, files, CsvRowMapper::mapFile);
        writeCsv(outputPath + "/class_metric.csv", CsvRowMapper.CLASS_HEADERS, classes, CsvRowMapper::mapClass);
        writeCsv(outputPath + "/method_metric.csv", CsvRowMapper.METHOD_HEADERS, methods, CsvRowMapper::mapMethod);
        writeCsv(outputPath + "/call_graph.csv", CsvRowMapper.CALL_HEADERS, calls, CsvRowMapper::mapCall);
    }

    private <T> void writeCsv(String filePath, String[] headers, List<T> data, RowMapper<T> mapper) throws IOException {
        try (FileWriter out = new FileWriter(filePath, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers))) {
            for (T item : data) {
                printer.printRecord(mapper.map(item));
            }
        }
    }

    @FunctionalInterface
    interface RowMapper<T> {
        Object[] map(T item);
    }
}
