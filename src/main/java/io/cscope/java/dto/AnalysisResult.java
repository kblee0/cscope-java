package io.cscope.java.dto;

import java.util.ArrayList;
import java.util.List;

/** 1차 파싱 결과 + 인메모리 후처리 결과를 담는 컨테이너. */
public class AnalysisResult {

    public ProjectMetric project;
    public final List<FileMetric> files = new ArrayList<>();
    public final List<ClassMetric> classes = new ArrayList<>();
    public final List<MethodMetric> methods = new ArrayList<>();
    public final List<CallEdge> calls = new ArrayList<>();
}
