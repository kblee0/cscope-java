package io.cscope.java.dto;

/** call_graph.callee_target_type 컬럼 값. */
public enum CalleeTargetType {
    /** 분석 대상 프로젝트 내부에서 method_metric 으로 수집된 메서드. */
    INTERNAL,
    /** 프로젝트/조직 소스로 보이지만 이번 분석 범위에서 메서드를 찾지 못한 호출. */
    EXTERNAL_PROJECT,
    /** 외부 라이브러리(바이너리) 호출. */
    LIBRARY
}
