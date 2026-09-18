# cscope-java

Eclipse JDT ASTParser 기반 Java 정적 분석기. 프로젝트를 파싱해 LoC / Cyclomatic Complexity / Call Graph 를 추출하고,
DB 5개 테이블과 1:1 대응하는 CSV 5개를 생성한다. (Bulk Import 전용 산출물)

## 1. 빌드

```
gradle jar
```

`build/libs/cscope-java.jar` (의존성 포함 실행 가능 Jar) 가 생성된다. JDK 17 이상 필요.

## 2. 실행

```
Usage: java -jar cscope-java.jar -p <project id> -s <path> [-l <jar_dir_or_file>] [-o <output path>] [-i <pkg1,pkg2>] [-e <pkg1,pkg2>]

  -p  project_id (필수). 예: my-app
  -s  분석 대상 소스 루트 디렉터리 (필수). file_path 의 기준 경로(package_path)가 된다.
  -l  의존 라이브러리 Jar 디렉터리 또는 Jar 파일 (선택)
  -o  CSV 출력 디렉터리 (선택, 기본 ./output)
  -i  수집 대상 패키지 prefix 목록, 콤마 구분 (선택)
  -e  제외 패키지 prefix 목록, 콤마 구분 (선택)
  -h  도움말
```

예시

```
java -jar cscope-java.jar -p my-app -s /work/my-app/src/main/java -l /work/my-app/libs -o ./output -i com.home -e com.home.legacy
```

> `-l` 을 생략하면 외부 타입 바인딩이 해석되지 않아 callee 클래스명이 부정확해질 수 있다(미해석 타입은 현재 패키지 기준으로
> 추정되거나 `<unresolved>` 로 기록된다). 정확한 Call Graph 가 필요하면 컴파일에 쓰는 의존 Jar 디렉터리를 반드시 지정한다.

## 3. 산출물

| 파일 | 테이블 | PK |
|---|---|---|
| `project.csv` | project | project_id |
| `file_metric.csv` | file_metric | file_id |
| `class_metric.csv` | class_metric | class_id |
| `method_metric.csv` | method_metric | method_id |
| `call_graph.csv` | call_graph | (caller_method_id, call_seq) |

CSV 포맷: UTF-8, 콤마 구분, 헤더 포함, LF 개행, 값에 콤마/개행/따옴표가 있을 때만 큰따옴표로 감싸고 내부 따옴표는 이중화(RFC4180).
`callee_method_id` 가 NULL 인 행은 따옴표 없는 빈 값으로 기록된다.

DDL 과 적재 쿼리는 `sql/` 에 있다.

| 파일 | 내용 |
|---|---|
| `sql/schema.sql` | MySQL 8.x DDL |
| `sql/schema_postgresql.sql` | PostgreSQL 13+ DDL |
| `sql/bulk_import.sql` | MySQL `LOAD DATA` / PostgreSQL `COPY` 예시 |

적재 순서는 FK 때문에 `project → file_metric → class_metric → method_metric → call_graph` 를 지켜야 한다.

## 4. 식별자 체계

| 항목 | 규칙 | 예시 |
|---|---|---|
| project_id | CLI `-p` 값 | `my-app` |
| file_id | `패키지명 + . + 파일명` | `com.home.service.UserService.java` |
| class_id | Full Qualified Class Name (중첩 클래스는 `Outer.Inner`) | `com.home.service.UserService` |
| method_id | `FQCN.메서드명(파라미터타입,...)` (제네릭 소거 기준 풀 타입명) | `com.home.service.UserService.createUser(java.lang.String,int)` |
| call_graph PK | `(caller_method_id, call_seq)` — call_seq 는 caller 내부 등장 순번 1부터 | |

`method_metric.signature` 는 `method_id` 와 같은 풀 시그니처 문자열이다. 후처리에서
`call_graph.callee_raw_signature` 와 문자열 동등 비교로 `callee_method_id` 를 연결하기 때문에 두 값의 생성 규칙을 동일하게 맞췄다.

## 5. 패키지 필터 (PackageFilter)

평가 우선순위

1. `-e` prefix 에 걸리면 무조건 제외
2. 기본 스킵 prefix (`java.`, `javax.`, `jakarta.`, `sun.`, `com.sun.`, `jdk.`, `org.springframework.`) 는 옵션과 무관하게 제외
3. `-i` 가 지정되면 해당 prefix 로 시작하는 callee 만 수집
4. `-i` 가 비어 있으면 1~2 를 제외한 전체 통과

필터는 **callee 클래스** 판정에만 쓰인다. 분석 대상 파일 수집은 `-s` 하위 전체 `.java` 다.

## 6. 계산 규칙

- **Cyclomatic Complexity**: `If, For, EnhancedFor, While, Do, Catch, Conditional(?:), SwitchCase(default 제외)` 개수 + 1
- **Fan-in**: 해당 method_id 가 `callee_method_id` 로 참조된 횟수
- **Fan-out**: 해당 method_id 가 `caller_method_id` 로 다른 메서드를 호출한 횟수
- **Weighted Complexity**: `round(cyclomatic_complexity + fan_in * 1.5 + fan_out * 1.0)`
- **class_metric**: 클래스 내 메서드 복잡도의 Total / Max / Avg(소수 2자리 반올림)
- **file_metric**: `total_lines` 전체 줄 수, `code_loc` 코드가 있는 줄, `comment_loc` 주석이 있는 줄
  (코드와 주석이 같은 줄에 있으면 양쪽 모두 계수, 빈 줄은 어느 쪽도 아님)
- **project.total_loc**: 전체 파일 `code_loc` 합계

### callee_target_type 판정

| 값 | 조건 |
|---|---|
| `INTERNAL` | `callee_raw_signature` 가 수집된 `method_metric.signature` 와 일치 |
| `EXTERNAL_PROJECT` | 매칭 실패 + (소스 기반 타입이거나, 수집된 class_id 이거나, `-i` prefix 매칭) |
| `LIBRARY` | 그 외 (바이너리 라이브러리 호출) |

## 7. 수집 범위 제약

- 호출 수집 노드: `MethodInvocation`, `SuperMethodInvocation`, `ClassInstanceCreation`.
  `this(...)` / `super(...)` 생성자 위임 호출은 수집하지 않는다.
- 익명/로컬 클래스의 메서드는 별도 `method_metric` 행으로 만들지 않고, 그 내부 호출을 바깥 named 메서드의 호출로 귀속시킨다.
- 필드 초기화식 / static 초기화 블록 안의 호출은 caller 메서드가 없으므로 수집하지 않는다.
- 묵시적(선언되지 않은) 기본 생성자는 `method_metric` 에 없으므로, 그 생성자 호출은 `EXTERNAL_PROJECT` 로 분류된다.

## 8. 자체 검증 결과

이 도구로 자기 자신(`src/main/java`)을 분석했을 때: files 24 / classes 26 / methods 84 / calls 204,
call_graph PK 유일성·call_seq 연속성·FK 정합성·fan_in/fan_out/weighted 재계산 일치 확인.
