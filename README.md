```
cscope-java/
├── build.gradle (Gradle 빌드 설정, JDT 및 Commons CSV 의존성 포함)
├── schema.sql (DB 테이블 DDL 및 Bulk Import 예시 쿼리)
└── src/main/java/io/cscope/java/
    ├── JdtProjectAnalyzerMain.java (CLI 실행 메인 클래스)
    ├── cli/
    │   ├── CliOptions.java (CLI 옵션 저장)
    │   └── CliOptionsParser.java (인수 파싱 로직)
    ├── config/
    │   ├── AnalyzerConfig.java (분석 환경 설정)
    │   └── PackageFilter.java (Include/Exclude 패키지 필터)
    ├── dto/
    │   ├── ProjectDto.java
    │   ├── FileMetricDto.java
    │   ├── ClassMetricDto.java
    │   ├── MethodMetricDto.java
    │   └── CallGraphDto.java
    ├── export/
    │   ├── CsvExportService.java (CSV 파일 생성 및 쓰기)
    │   └── CsvRowMapper.java (데이터 행 매핑 정의)
    ├── parser/
    │   ├── CyclomaticComplexityVisitor.java (순환 복잡도 계산 엔진)
    │   ├── ExtendedJdtMetricsVisitor.java (AST 기반 메트릭/호출 추출)
    │   └── JdtParserFactory.java (ASTParser 설정 및 생성)
    ├── service/
    │   ├── CallGraphResolver.java (Callee ID 및 Target Type 확인)
    │   ├── MetricAggregator.java (Fan-in/out 및 가중 복잡도 연산)
    │   └── ProjectAnalyzerService.java (분석 프로세스 총괄)
    └── util/
        ├── LineNumberUtils.java (LoC 및 라인 번호 유틸)
        └── SourceIdBuilder.java (Natural Business Key 생성기)
```
  구현 핵심 요약
   1. ID 체계: SourceIdBuilder를 통해 package.class.method(params) 형태의 문자열 자연 키를 생성하여 DB Join 및 분석
      용이성을 극대화했습니다.
   2. 순환 복잡도: CyclomaticComplexityVisitor가 If, For, While, Catch, SwitchCase, &&/|| 등을 추적하여 표준
      계산법(Nodes + 1)을 적용합니다.
   3. 인메모리 후처리: ProjectAnalyzerService에서 1차 파싱 후 CallGraphResolver와 MetricAggregator를 호출하여 전체
      프로젝트 맥락에서의 Fan-in, Fan-out, 가중 복잡도를 산출합니다.
   4. CSV 매핑: CsvExportService는 생성된 DTO 리스트를 schema.sql 정의와 100% 일치하는 헤더와 데이터 구조로 출력합니다.
   
  2. 분석 실행 (Run)
  CLI 옵션을 사용하여 대상 Java 프로젝트를 분석합니다.

  기본 실행 예시

   1 java -jar build/libs/cscope-java-1.0-SNAPSHOT.jar \
   2   --project "my-awesome-app" \
   3   --source "C:/path/to/your/java/project/src/main/java" \
   4   --output "./analysis_results"

  옵션 상세 설명
  | 옵션      | 필수 여부 | 설명                                      | 예시                                        |
  | --------- | --------- | ----------------------------------------- | ------------------------------------------- |
  | --project | 필수      | 프로젝트 고유 식별자 (DB의 project_id)    | erp-system                                  |
  | --source  | 필수      | 분석할 Java 소스 코드가 있는 최상위 경로  | /home/dev/src                               |
  | --output  | 선택      | CSV 파일이 저장될 폴더 (기본값: ./output) | ./results                                   |
  | --include | 선택      | 분석 대상 패키지 접두사 (콤마 구분)       | com.mycompany                               |
  | --exclude | 선택      | 분석 제외 패키지 접두사 (콤마 구분)       | com.mycompany.test                          |
  | --libs    | 선택      | Class Path                                | a.jar;/home/dev/prj/build/classes/java/main |
  
---

  3. 결과 확인 (Output)
  실행이 완료되면 지정한 --output 폴더에 다음 5개 파일이 생성됩니다.

   1. project.csv: 프로젝트 요약 메트릭
   2. file_metric.csv: 파일 단위 LoC 및 통계
   3. class_metric.csv: 클래스 타입 및 복잡도 통계
   4. method_metric.csv: 메서드 시그니처, 복잡도, Fan-in/out
   5. call_graph.csv: 메서드 간 호출 관계 (Internal/Library 구분)

  ---

  4. DB 데이터 임포트 (Bulk Import)
  생성된 CSV 파일을 DB에 수작업으로 적재할 때는 schema.sql에 포함된 쿼리를 참조하십시오.

  MySQL 예시

   1 -- 데이터 적재 순서 주의 (FK 제약 조건)
   2 -- project -> file -> class -> method -> call_graph 순으로 진행
   3 LOAD DATA INFILE '/path/to/method_metric.csv'
   4 INTO TABLE method_metric
   5 FIELDS TERMINATED BY ','
   6 ENCLOSED BY '"'
   7 IGNORE 1 LINES;

  PostgreSQL 예시

   1 psql -d my_database -c "\copy method_metric FROM 'method_metric.csv' WITH (FORMAT csv, HEADER true, QUOTE '\"')"

  ---

  5. 주요 분석 로직 팁
   * 복잡도(Cyclomatic Complexity): 메서드 내의 조건문, 반복문, 논리 연산자(&&, ||)를 추적하여 계산합니다.
   * 가중 복잡도(Weighted Complexity): 단순 로직 복잡도에 외부 영향도(Fan-in/out)를 더해 산출하므로, 리팩토링이 시급한
     "God Method"를 찾기에 적합합니다.
   * Call Graph: 라이브러리 호출은 callee_target_type = 'LIBRARY'로 표시되어 내부 로직 분석 시 필터링할 수 있습니다.
