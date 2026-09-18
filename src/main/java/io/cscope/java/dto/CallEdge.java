package io.cscope.java.dto;

/** call_graph.csv 1 행. PK 는 (callerMethodId, callSeq) 복합키. */
public class CallEdge {

    public String callerMethodId;
    public int callSeq;
    public String projectId;
    /** 후처리에서 확정. INTERNAL 이 아니면 null. */
    public String calleeMethodId;
    public CalleeTargetType calleeTargetType;
    public String calleeClassName;
    public String calleeMethodName;
    public String calleeRawSignature;
    public int callLine;

    /** CSV 출력 대상 아님. callee 타입 확정(EXTERNAL_PROJECT / LIBRARY)에만 사용. */
    public boolean calleeFromSource;
}
