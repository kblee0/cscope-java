package io.cscope.java.export;

import io.cscope.java.dto.CallEdge;
import io.cscope.java.dto.ClassMetric;
import io.cscope.java.dto.FileMetric;
import io.cscope.java.dto.MethodMetric;
import io.cscope.java.dto.ProjectMetric;

/** DTO → CSV 행 매핑. 헤더 순서와 행 순서는 반드시 1:1 로 유지한다. */
public final class CsvRowMapper {

    public static final String[] PROJECT_HEADER = {
            "project_id", "project_name", "analyzed_at", "package_path",
            "total_files", "total_loc", "total_classes", "total_methods" };

    public static final String[] FILE_HEADER = {
            "file_id", "project_id", "package_name", "file_name", "file_path",
            "total_lines", "code_loc", "comment_loc", "class_count", "method_count" };

    public static final String[] CLASS_HEADER = {
            "class_id", "project_id", "file_id", "package_name", "class_name", "full_class_name",
            "class_type", "start_line", "end_line", "loc", "method_count",
            "total_cyclomatic_complexity", "max_cyclomatic_complexity", "avg_cyclomatic_complexity" };

    public static final String[] METHOD_HEADER = {
            "method_id", "project_id", "class_id", "package_name", "class_name", "method_name",
            "signature", "start_line", "end_line", "loc", "cyclomatic_complexity",
            "fan_in", "fan_out", "weighted_complexity" };

    public static final String[] CALL_GRAPH_HEADER = {
            "caller_method_id", "call_seq", "project_id", "callee_method_id", "callee_target_type",
            "callee_class_name", "callee_method_name", "callee_raw_signature", "call_line" };

    private CsvRowMapper() {
    }

    public static Object[] toRow(ProjectMetric project) {
        return new Object[] {
                project.projectId, project.projectName, project.analyzedAt, project.packagePath,
                project.totalFiles, project.totalLoc, project.totalClasses, project.totalMethods };
    }

    public static Object[] toRow(FileMetric file) {
        return new Object[] {
                file.fileId, file.projectId, file.packageName, file.fileName, file.filePath,
                file.totalLines, file.codeLoc, file.commentLoc, file.classCount, file.methodCount };
    }

    public static Object[] toRow(ClassMetric clazz) {
        return new Object[] {
                clazz.classId, clazz.projectId, clazz.fileId, clazz.packageName, clazz.className,
                clazz.fullClassName, clazz.classType, clazz.startLine, clazz.endLine, clazz.loc,
                clazz.methodCount, clazz.totalCyclomaticComplexity, clazz.maxCyclomaticComplexity,
                clazz.avgCyclomaticComplexity };
    }

    public static Object[] toRow(MethodMetric method) {
        return new Object[] {
                method.methodId, method.projectId, method.classId, method.packageName, method.className,
                method.methodName, method.signature, method.startLine, method.endLine, method.loc,
                method.cyclomaticComplexity, method.fanIn, method.fanOut, method.weightedComplexity };
    }

    public static Object[] toRow(CallEdge edge) {
        return new Object[] {
                edge.callerMethodId, edge.callSeq, edge.projectId, edge.calleeMethodId, edge.calleeTargetType,
                edge.calleeClassName, edge.calleeMethodName, edge.calleeRawSignature, edge.callLine };
    }
}
