package io.cscope.java.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * callee 클래스 수집 여부 판정 필터.
 *
 * <p>평가 우선순위
 * <ol>
 *   <li>excludePrefixes 에 걸리면 무조건 제외</li>
 *   <li>JDK / Spring 등 기본 스킵 prefix 에 걸리면 제외</li>
 *   <li>includePrefixes 가 지정되어 있으면 매칭될 때만 수집</li>
 *   <li>includePrefixes 가 비어 있으면 위 항목을 제외한 전체 통과</li>
 * </ol>
 */
public class PackageFilter {

    /** includePrefixes / excludePrefixes 와 무관하게 기본 스킵되는 prefix. */
    public static final List<String> DEFAULT_SKIP_PREFIXES = List.of(
            "java.",
            "javax.",
            "jakarta.",
            "sun.",
            "com.sun.",
            "jdk.",
            "org.springframework.");

    private final List<String> includePrefixes;
    private final List<String> excludePrefixes;

    public PackageFilter(List<String> includePrefixes, List<String> excludePrefixes) {
        this.includePrefixes = normalize(includePrefixes);
        this.excludePrefixes = normalize(excludePrefixes);
    }

    /** 해당 클래스로의 호출을 call_graph 에 수집할지 여부. */
    public boolean isCollectable(String className) {
        if (className == null || className.isEmpty()) {
            return false;
        }
        for (String prefix : excludePrefixes) {
            if (className.startsWith(prefix)) {
                return false;
            }
        }
        for (String prefix : DEFAULT_SKIP_PREFIXES) {
            if (className.startsWith(prefix)) {
                return false;
            }
        }
        if (includePrefixes.isEmpty()) {
            return true;
        }
        return matchesInclude(className);
    }

    /** includePrefixes 중 하나로 시작하는지 여부. includePrefixes 가 비어 있으면 false. */
    public boolean matchesInclude(String className) {
        if (className == null || className.isEmpty()) {
            return false;
        }
        for (String prefix : includePrefixes) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getIncludePrefixes() {
        return includePrefixes;
    }

    public List<String> getExcludePrefixes() {
        return excludePrefixes;
    }

    private static List<String> normalize(List<String> prefixes) {
        if (prefixes == null || prefixes.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        for (String prefix : prefixes) {
            String trimmed = prefix.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        return Collections.unmodifiableList(normalized);
    }
}
