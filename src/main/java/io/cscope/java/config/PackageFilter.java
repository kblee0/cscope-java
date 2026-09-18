package io.cscope.java.config;

import java.util.ArrayList;
import java.util.List;

public class PackageFilter {
    private final List<String> includePrefixes;
    private final List<String> excludePrefixes;

    public PackageFilter(List<String> includePrefixes, List<String> excludePrefixes) {
        this.includePrefixes = includePrefixes != null ? includePrefixes : new ArrayList<>();
        this.excludePrefixes = excludePrefixes != null ? new ArrayList<>(excludePrefixes) : new ArrayList<>();
        
        // --- Default exclusions (Standard Libraries & Frameworks) ---
        // JDK / JRE
        this.excludePrefixes.add("java.");
        this.excludePrefixes.add("javax.");
        this.excludePrefixes.add("jakarta.");
        this.excludePrefixes.add("sun.");
        this.excludePrefixes.add("jdk.");
        this.excludePrefixes.add("com.sun.");
        this.excludePrefixes.add("netscape.");
        
        // Spring Framework & Common Ecosystem
        this.excludePrefixes.add("org.springframework.");
        this.excludePrefixes.add("org.apache.commons.");
        this.excludePrefixes.add("org.apache.logging.");
        this.excludePrefixes.add("org.slf4j.");
        this.excludePrefixes.add("ch.qos.logback.");
        
        // Common Libraries
        this.excludePrefixes.add("org.hibernate.");
        this.excludePrefixes.add("com.google.common."); // Guava
        this.excludePrefixes.add("com.fasterxml.jackson.");
        this.excludePrefixes.add("com.gson.");
        
        // Testing Frameworks
        this.excludePrefixes.add("org.junit.");
        this.excludePrefixes.add("org.mockito.");
        this.excludePrefixes.add("org.assertj.");
    }

    public boolean shouldAnalyze(String packageName) {
        if (packageName == null) return true;

        for (String exclude : excludePrefixes) {
            if (packageName.startsWith(exclude)) {
                return false;
            }
        }

        if (includePrefixes.isEmpty()) {
            return true;
        }

        for (String include : includePrefixes) {
            if (packageName.startsWith(include)) {
                return true;
            }
        }

        return false;
    }
}
