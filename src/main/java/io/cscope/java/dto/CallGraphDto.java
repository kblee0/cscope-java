package io.cscope.java.dto;

public class CallGraphDto {
    public String callerMethodId;
    public int callSeq;
    public String projectId;
    public String calleeMethodId;
    public String calleeTargetType;
    public String calleeClassName;
    public String calleeMethodName;
    public String calleeRawSignature;
    public int callLine;

    public CallGraphDto(String callerMethodId, int callSeq, String projectId) {
        this.callerMethodId = callerMethodId;
        this.callSeq = callSeq;
        this.projectId = projectId;
    }
}
