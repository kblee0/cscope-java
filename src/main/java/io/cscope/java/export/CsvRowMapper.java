package io.cscope.java.export;

import io.cscope.java.dto.*;

public class CsvRowMapper {
    public static final String[] PROJECT_HEADERS = {"project_id", "project_name", "analyzed_at", "package_path", "total_files", "total_loc", "total_classes", "total_methods"};
    public static final String[] FILE_HEADERS = {"file_id", "project_id", "package_name", "file_name", "file_path", "total_lines", "code_loc", "comment_loc", "class_count", "method_count"};
    public static final String[] CLASS_HEADERS = {"class_id", "project_id", "file_id", "package_name", "class_name", "full_class_name", "class_type", "start_line", "end_line", "loc", "method_count", "total_cyclomatic_complexity", "max_cyclomatic_complexity", "avg_cyclomatic_complexity"};
    public static final String[] METHOD_HEADERS = {"method_id", "project_id", "class_id", "package_name", "class_name", "method_name", "signature", "start_line", "end_line", "loc", "cyclomatic_complexity", "fan_in", "fan_out", "weighted_complexity"};
    public static final String[] CALL_HEADERS = {"caller_method_id", "call_seq", "project_id", "callee_method_id", "callee_target_type", "callee_class_name", "callee_method_name", "callee_raw_signature", "call_line"};

    public static Object[] mapProject(ProjectDto p) {
        return new Object[]{p.projectId, p.projectName, p.analyzedAt, p.packagePath, p.totalFiles, p.totalLoc, p.totalClasses, p.totalMethods};
    }

    public static Object[] mapFile(FileMetricDto f) {
        return new Object[]{f.fileId, f.projectId, f.packageName, f.fileName, f.filePath, f.totalLines, f.codeLoc, f.commentLoc, f.classCount, f.methodCount};
    }

    public static Object[] mapClass(ClassMetricDto c) {
        return new Object[]{c.classId, c.projectId, c.fileId, c.packageName, c.className, c.fullClassName, c.classType, c.startLine, c.endLine, c.loc, c.methodCount, c.totalCyclomaticComplexity, c.maxCyclomaticComplexity, c.avgCyclomaticComplexity};
    }

    public static Object[] mapMethod(MethodMetricDto m) {
        return new Object[]{m.methodId, m.projectId, m.classId, m.packageName, m.className, m.methodName, m.signature, m.startLine, m.endLine, m.loc, m.cyclomaticComplexity, m.fanIn, m.fanOut, m.weightedComplexity};
    }

    public static Object[] mapCall(CallGraphDto c) {
        return new Object[]{c.callerMethodId, c.callSeq, c.projectId, c.calleeMethodId, c.calleeTargetType, c.calleeClassName, c.calleeMethodName, c.calleeRawSignature, c.callLine};
    }
}
