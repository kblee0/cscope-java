-- =====================================================================
-- cscope-java : CSV Bulk Import 예시
-- 적재 순서는 FK 때문에 project → file_metric → class_metric → method_metric → call_graph
-- call_graph 는 caller/callee 가 모두 method_metric 에 있어야 하므로 반드시 마지막
-- =====================================================================

-- =====================================================================
-- [A] MySQL 8.x : LOAD DATA
--   - 서버/클라이언트에 local_infile=1 필요 (mysql --local-infile=1 ...)
--   - CSV 는 UTF-8, LF 개행, RFC4180(따옴표 이중화) 형식
--   - 빈 문자열은 NULLIF 로 NULL 변환
-- =====================================================================

SET FOREIGN_KEY_CHECKS = 0;

LOAD DATA LOCAL INFILE '/data/cscope/output/project.csv'
INTO TABLE project
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' ENCLOSED BY '"' ESCAPED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(project_id, project_name, @analyzed_at, package_path,
 total_files, total_loc, total_classes, total_methods)
SET analyzed_at = NULLIF(@analyzed_at, '');

LOAD DATA LOCAL INFILE '/data/cscope/output/file_metric.csv'
INTO TABLE file_metric
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' ENCLOSED BY '"' ESCAPED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(file_id, project_id, package_name, file_name, file_path,
 total_lines, code_loc, comment_loc, class_count, method_count);

LOAD DATA LOCAL INFILE '/data/cscope/output/class_metric.csv'
INTO TABLE class_metric
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' ENCLOSED BY '"' ESCAPED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(class_id, project_id, file_id, package_name, class_name, full_class_name,
 class_type, start_line, end_line, loc, method_count,
 total_cyclomatic_complexity, max_cyclomatic_complexity, avg_cyclomatic_complexity);

LOAD DATA LOCAL INFILE '/data/cscope/output/method_metric.csv'
INTO TABLE method_metric
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' ENCLOSED BY '"' ESCAPED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(method_id, project_id, class_id, package_name, class_name, method_name,
 signature, start_line, end_line, loc, cyclomatic_complexity,
 fan_in, fan_out, weighted_complexity);

LOAD DATA LOCAL INFILE '/data/cscope/output/call_graph.csv'
INTO TABLE call_graph
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' ENCLOSED BY '"' ESCAPED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(caller_method_id, call_seq, project_id, @callee_method_id, callee_target_type,
 callee_class_name, callee_method_name, callee_raw_signature, call_line)
SET callee_method_id = NULLIF(@callee_method_id, '');

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- [B] PostgreSQL 13+ : COPY
--   - 서버 측 파일이면 COPY, 클라이언트(psql) 측 파일이면 \copy
--   - FORMAT csv + HEADER true, 따옴표 없는 빈 값은 NULL 로 인식
-- =====================================================================

-- psql 클라이언트에서 실행하는 경우 (\copy 는 한 줄로 작성해야 함)
-- \copy project (project_id, project_name, analyzed_at, package_path, total_files, total_loc, total_classes, total_methods) FROM '/data/cscope/output/project.csv' WITH (FORMAT csv, HEADER true, NULL '')
-- \copy file_metric (file_id, project_id, package_name, file_name, file_path, total_lines, code_loc, comment_loc, class_count, method_count) FROM '/data/cscope/output/file_metric.csv' WITH (FORMAT csv, HEADER true, NULL '')
-- \copy class_metric (class_id, project_id, file_id, package_name, class_name, full_class_name, class_type, start_line, end_line, loc, method_count, total_cyclomatic_complexity, max_cyclomatic_complexity, avg_cyclomatic_complexity) FROM '/data/cscope/output/class_metric.csv' WITH (FORMAT csv, HEADER true, NULL '')
-- \copy method_metric (method_id, project_id, class_id, package_name, class_name, method_name, signature, start_line, end_line, loc, cyclomatic_complexity, fan_in, fan_out, weighted_complexity) FROM '/data/cscope/output/method_metric.csv' WITH (FORMAT csv, HEADER true, NULL '')
-- \copy call_graph (caller_method_id, call_seq, project_id, callee_method_id, callee_target_type, callee_class_name, callee_method_name, callee_raw_signature, call_line) FROM '/data/cscope/output/call_graph.csv' WITH (FORMAT csv, HEADER true, NULL '')

-- 서버 측 파일로 실행하는 경우
COPY project (project_id, project_name, analyzed_at, package_path,
              total_files, total_loc, total_classes, total_methods)
FROM '/data/cscope/output/project.csv' WITH (FORMAT csv, HEADER true, NULL '');

COPY file_metric (file_id, project_id, package_name, file_name, file_path,
                  total_lines, code_loc, comment_loc, class_count, method_count)
FROM '/data/cscope/output/file_metric.csv' WITH (FORMAT csv, HEADER true, NULL '');

COPY class_metric (class_id, project_id, file_id, package_name, class_name, full_class_name,
                   class_type, start_line, end_line, loc, method_count,
                   total_cyclomatic_complexity, max_cyclomatic_complexity, avg_cyclomatic_complexity)
FROM '/data/cscope/output/class_metric.csv' WITH (FORMAT csv, HEADER true, NULL '');

COPY method_metric (method_id, project_id, class_id, package_name, class_name, method_name,
                    signature, start_line, end_line, loc, cyclomatic_complexity,
                    fan_in, fan_out, weighted_complexity)
FROM '/data/cscope/output/method_metric.csv' WITH (FORMAT csv, HEADER true, NULL '');

COPY call_graph (caller_method_id, call_seq, project_id, callee_method_id, callee_target_type,
                 callee_class_name, callee_method_name, callee_raw_signature, call_line)
FROM '/data/cscope/output/call_graph.csv' WITH (FORMAT csv, HEADER true, NULL '');
