-- =====================================================================
-- cscope-java : 정적 분석 메트릭 스키마 (MySQL 8.x)
-- 컬럼 순서 = CSV 컬럼 순서 (LOAD DATA 시 컬럼 목록 생략 가능하도록 일치시킴)
-- 식별자는 모두 자연 키(VARCHAR), call_graph 는 (caller_method_id, call_seq) 복합키
-- =====================================================================

DROP TABLE IF EXISTS call_graph;
DROP TABLE IF EXISTS method_metric;
DROP TABLE IF EXISTS class_metric;
DROP TABLE IF EXISTS file_metric;
DROP TABLE IF EXISTS project;

-- ---------------------------------------------------------------------
-- 1. project
-- ---------------------------------------------------------------------
CREATE TABLE project (
    project_id      VARCHAR(100) NOT NULL,
    project_name    VARCHAR(100) NULL,
    analyzed_at     TIMESTAMP    NULL,
    package_path    VARCHAR(500) NULL,
    total_files     INT          NULL,
    total_loc       INT          NULL,
    total_classes   INT          NULL,
    total_methods   INT          NULL,
    CONSTRAINT pk_project PRIMARY KEY (project_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC;

-- ---------------------------------------------------------------------
-- 2. file_metric
-- ---------------------------------------------------------------------
CREATE TABLE file_metric (
    file_id         VARCHAR(500) NOT NULL,
    project_id      VARCHAR(100) NOT NULL,
    package_name    VARCHAR(255) NULL,
    file_name       VARCHAR(255) NULL,
    file_path       VARCHAR(500) NULL,
    total_lines     INT          NULL,
    code_loc        INT          NULL,
    comment_loc     INT          NULL,
    class_count     INT          NULL,
    method_count    INT          NULL,
    CONSTRAINT pk_file_metric PRIMARY KEY (file_id),
    CONSTRAINT fk_file_metric_project FOREIGN KEY (project_id) REFERENCES project (project_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC;

CREATE INDEX ix_file_metric_project ON file_metric (project_id);

-- ---------------------------------------------------------------------
-- 3. class_metric
-- ---------------------------------------------------------------------
CREATE TABLE class_metric (
    class_id                    VARCHAR(255) NOT NULL,
    project_id                  VARCHAR(100) NOT NULL,
    file_id                     VARCHAR(500) NOT NULL,
    package_name                VARCHAR(255) NULL,
    class_name                  VARCHAR(255) NULL,
    full_class_name             VARCHAR(255) NULL,
    class_type                  VARCHAR(50)  NULL, -- CLASS / INTERFACE / ENUM / ABSTRACT_CLASS
    start_line                  INT          NULL,
    end_line                    INT          NULL,
    loc                         INT          NULL,
    method_count                INT          NULL,
    total_cyclomatic_complexity INT          NULL,
    max_cyclomatic_complexity   INT          NULL,
    avg_cyclomatic_complexity   DOUBLE       NULL,
    CONSTRAINT pk_class_metric PRIMARY KEY (class_id),
    CONSTRAINT fk_class_metric_project FOREIGN KEY (project_id) REFERENCES project (project_id),
    CONSTRAINT fk_class_metric_file FOREIGN KEY (file_id) REFERENCES file_metric (file_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC;

CREATE INDEX ix_class_metric_project ON class_metric (project_id);
CREATE INDEX ix_class_metric_file ON class_metric (file_id);

-- ---------------------------------------------------------------------
-- 4. method_metric
-- ---------------------------------------------------------------------
CREATE TABLE method_metric (
    method_id             VARCHAR(500) NOT NULL,
    project_id            VARCHAR(100) NOT NULL,
    class_id              VARCHAR(255) NOT NULL,
    package_name          VARCHAR(255) NULL,
    class_name            VARCHAR(255) NULL,
    method_name           VARCHAR(255) NULL,
    signature             VARCHAR(500) NULL,
    start_line            INT          NULL,
    end_line              INT          NULL,
    loc                   INT          NULL,
    cyclomatic_complexity INT          NULL,
    fan_in                INT          NULL,
    fan_out               INT          NULL,
    weighted_complexity   INT          NULL,
    CONSTRAINT pk_method_metric PRIMARY KEY (method_id),
    CONSTRAINT fk_method_metric_project FOREIGN KEY (project_id) REFERENCES project (project_id),
    CONSTRAINT fk_method_metric_class FOREIGN KEY (class_id) REFERENCES class_metric (class_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC;

CREATE INDEX ix_method_metric_project ON method_metric (project_id);
CREATE INDEX ix_method_metric_class ON method_metric (class_id);

-- ---------------------------------------------------------------------
-- 5. call_graph : PK = (caller_method_id, call_seq)
-- ---------------------------------------------------------------------
CREATE TABLE call_graph (
    caller_method_id      VARCHAR(500) NOT NULL,
    call_seq              INT          NOT NULL,
    project_id            VARCHAR(100) NOT NULL,
    callee_method_id      VARCHAR(500) NULL,
    callee_target_type    VARCHAR(50)  NULL, -- INTERNAL / EXTERNAL_PROJECT / LIBRARY
    callee_class_name     VARCHAR(255) NULL,
    callee_method_name    VARCHAR(255) NULL,
    callee_raw_signature  VARCHAR(500) NULL,
    call_line             INT          NULL,
    CONSTRAINT pk_call_graph PRIMARY KEY (caller_method_id, call_seq),
    CONSTRAINT fk_call_graph_project FOREIGN KEY (project_id) REFERENCES project (project_id),
    CONSTRAINT fk_call_graph_caller FOREIGN KEY (caller_method_id) REFERENCES method_metric (method_id),
    CONSTRAINT fk_call_graph_callee FOREIGN KEY (callee_method_id) REFERENCES method_metric (method_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC;

CREATE INDEX ix_call_graph_project ON call_graph (project_id);
CREATE INDEX ix_call_graph_callee ON call_graph (callee_method_id);
CREATE INDEX ix_call_graph_callee_class ON call_graph (callee_class_name);
