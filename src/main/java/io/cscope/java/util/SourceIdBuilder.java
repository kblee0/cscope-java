package io.cscope.java.util;

public class SourceIdBuilder {
    public static String buildFileId(String packageName, String fileName) {
        String prefix = (packageName == null || packageName.isEmpty()) ? "" : packageName + ".";
        return prefix + fileName;
    }

    public static String buildClassId(String packageName, String className) {
        String prefix = (packageName == null || packageName.isEmpty()) ? "" : packageName + ".";
        return prefix + className;
    }

    public static String buildMethodId(String classId, String methodName, String signature) {
        // signature usually contains param types like (java.lang.String,int)
        return classId + "." + methodName + signature;
    }
}
