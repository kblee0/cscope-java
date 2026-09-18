package io.cscope.java.cli;

import java.util.ArrayList;
import java.util.List;

/** CLI 인자 원본 값 홀더. */
public class CliOptions {

    /** -p : project_id (필수) */
    public String projectId;
    /** -s : 분석 대상 소스 루트 (필수) */
    public String sourcePath;
    /** -l : 의존 Jar 디렉터리 또는 Jar 파일 (선택) */
    public String libPath;
    /** -o : CSV 출력 디렉터리 (기본 ./output) */
    public String outputPath = "output";
    /** -i : include 패키지 prefix 목록 */
    public final List<String> includePrefixes = new ArrayList<>();
    /** -e : exclude 패키지 prefix 목록 */
    public final List<String> excludePrefixes = new ArrayList<>();
    /** -h */
    public boolean help;
}
