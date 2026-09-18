-- =====================================================================
-- cscope-java : 정적 분석 메트릭 스키마 (PostgreSQL 13+)
-- 컬럼 순서 = CSV 컬럼 순서
-- =====================================================================

DROP TABLE IF EXISTS call_graph;
DROP TABLE IF EXISTS method_metric;
DROP TABLE IF EXISTS class_metric;
DROP TABLE IF EXISTS file_metric;
DROP TABLE IF EXISTS project;

CREATE TABLE project (
    project_id      VARCHAR(100) NOT NULL,
    project_name    VARCHAR(100),
    analyzed_at     TIMESTAMP,
    package_path    VARCHAR(500),
    total_files     INTEGER,
    total_loc       INTEGER,
    total_classes   INTEGER,
    total_methods   INTEGER,
    CONSTRAINT pk_project PRIMARY KEY (project_id)
);

CREATE TABLE file_metric (
    file_id         VARCHAR(500) NOT NULL,
    project_id      VARCHAR(100) NOT NULL,
    package_name    VARCHAR(255),
    file_name       VARCHAR(255),
    file_path       VARCHAR(500),
    total_lines     INTEGER,
    code_loc        INTEGER,
    comment_loc     INTEGER,
    class_count     INTEGER,
    method_count    INTEGER,
    CONSTRAINT pk_file_metric PRIMARY KEY (file_id),
    CONSTRAINT fk_file_metric_project FOREIGN KEY (project_id) REFERENCES project (project_id)
);

CREATE INDEX ix_file_metric_project ON file_metric (project_id);

CREATE TABLE class_metric (
    class_id                    VARCHAR(255) NOT NULL,
    project_id                  VARCHAR(100) NOT NULL,
    file_id                     VARCHAR(500) NOT NULL,
    package_name                VARCHAR(255),
    class_name                  VARCHAR(255),
    full_class_name             VARCHAR(255),
    class_type                  VARCHAR(50),  -- CLASS / INTERFACE / ENUM / ABSTRACT_CLASS
    start_line                  INTEGER,
    end_line                    INTEGER,
    loc                         INTEGER,
    method_count                INTEGER,
    total_cyclomatic_complexity INTEGER,
    max_cyclomatic_complexity   INTEGER,
    avg_cyclomatic_complexity   DOUBLE PRECISION,
    CONSTRAINT pk_class_metric PRIMARY KEY (class_id),
    CONSTRAINT fk_class_metric_project FOREIGN KEY (project_id) REFERENCES project (project_id),
    CONSTRAINT fk_class_metric_file FOREIGN KEY (file_id) REFERENCES file_metric (file_id)
);

CREATE INDEX ix_class_metric_project ON class_metric (project_id);
CREATE INDEX ix_class_metric_file ON class_metric (file_id);

CREATE TABLE method_metric (
    method_id             VARCHAR(500) NOT NULL,
    project_id            VARCHAR(100) NOT NULL,
    class_id              VARCHAR(255) NOT NULL,
    package_name          VARCHAR(255),
    class_name            VARCHAR(255),
    method_name           VARCHAR(255),
    signature             VARCHAR(500),
    start_line            INTEGER,
    end_line              INTEGER,
    loc                   INTEGER,
    cyclomatic_complexity INTEGER,
    fan_in                INTEGER,
    fan_out               INTEGER,
    weighted_complexity   INTEGER,
    CONSTRAINT pk_method_metric PRIMARY KEY (method_id),
    CONSTRAINT fk_method_metric_project FOREIGN KEY (project_id) REFERENCES project (project_id),
    CONSTRAINT fk_method_metric_class FOREIGN KEY (class_id) REFERENCES class_metric (class_id)
);

CREATE INDEX ix_method_metric_project ON method_metric (project_id);
CREATE INDEX ix_method_metric_class ON method_metric (class_id);

CREATE TABLE call_graph (
    caller_method_id      VARCHAR(500) NOT NULL,
    call_seq              INTEGER      NOT NULL,
    project_id            VARCHAR(100) NOT NULL,
    callee_method_id      VARCHAR(500),
    callee_target_type    VARCHAR(50),  -- INTERNAL / EXTERNAL_PROJECT / LIBRARY
    callee_class_name     VARCHAR(255),
    callee_method_name    VARCHAR(255),
    callee_raw_signature  VARCHAR(500),
    call_line             INTEGER,
    CONSTRAINT pk_call_graph PRIMARY KEY (caller_method_id, call_seq),
    CONSTRAINT fk_call_graph_project FOREIGN KEY (project_id) REFERENCES project (project_id),
    CONSTRAINT fk_call_graph_caller FOREIGN KEY (caller_method_id) REFERENCES method_metric (method_id),
    CONSTRAINT fk_call_graph_callee FOREIGN KEY (callee_method_id) REFERENCES method_metric (method_id)
);

CREATE INDEX ix_call_graph_project ON call_graph (project_id);
CREATE INDEX ix_call_graph_callee ON call_graph (callee_method_id);
CREATE INDEX ix_call_graph_callee_class ON call_graph (callee_class_name);
