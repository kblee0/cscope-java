-- Schema for cscope-java Analysis Results

CREATE TABLE IF NOT EXISTS project (
    project_id VARCHAR(100) PRIMARY KEY,
    project_name VARCHAR(100) NOT NULL,
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    package_path VARCHAR(500),
    total_files INT DEFAULT 0,
    total_loc INT DEFAULT 0,
    total_classes INT DEFAULT 0,
    total_methods INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS file_metric (
    file_id VARCHAR(500) PRIMARY KEY,
    project_id VARCHAR(100) NOT NULL,
    package_name VARCHAR(255),
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    total_lines INT DEFAULT 0,
    code_loc INT DEFAULT 0,
    comment_loc INT DEFAULT 0,
    class_count INT DEFAULT 0,
    method_count INT DEFAULT 0,
    CONSTRAINT fk_file_project FOREIGN KEY (project_id) REFERENCES project(project_id)
);

CREATE TABLE IF NOT EXISTS class_metric (
    class_id VARCHAR(255) PRIMARY KEY,
    project_id VARCHAR(100) NOT NULL,
    file_id VARCHAR(500) NOT NULL,
    package_name VARCHAR(255),
    class_name VARCHAR(255),
    full_class_name VARCHAR(255),
    class_type VARCHAR(50),
    start_line INT,
    end_line INT,
    loc INT,
    method_count INT,
    total_cyclomatic_complexity INT,
    max_cyclomatic_complexity INT,
    avg_cyclomatic_complexity DOUBLE,
    CONSTRAINT fk_class_project FOREIGN KEY (project_id) REFERENCES project(project_id),
    CONSTRAINT fk_class_file FOREIGN KEY (file_id) REFERENCES file_metric(file_id)
);

CREATE TABLE IF NOT EXISTS method_metric (
    method_id VARCHAR(500) PRIMARY KEY,
    project_id VARCHAR(100) NOT NULL,
    class_id VARCHAR(255) NOT NULL,
    package_name VARCHAR(255),
    class_name VARCHAR(255),
    method_name VARCHAR(255),
    signature VARCHAR(500),
    start_line INT,
    end_line INT,
    loc INT,
    cyclomatic_complexity INT,
    fan_in INT DEFAULT 0,
    fan_out INT DEFAULT 0,
    weighted_complexity INT,
    CONSTRAINT fk_method_project FOREIGN KEY (project_id) REFERENCES project(project_id),
    CONSTRAINT fk_method_class FOREIGN KEY (class_id) REFERENCES class_metric(class_id)
);

CREATE TABLE IF NOT EXISTS call_graph (
    caller_method_id VARCHAR(500) NOT NULL,
    call_seq INT NOT NULL,
    project_id VARCHAR(100) NOT NULL,
    callee_method_id VARCHAR(500),
    callee_target_type VARCHAR(50),
    callee_class_name VARCHAR(255),
    callee_method_name VARCHAR(255),
    callee_raw_signature VARCHAR(500),
    call_line INT,
    PRIMARY KEY (caller_method_id, call_seq),
    CONSTRAINT fk_call_caller FOREIGN KEY (caller_method_id) REFERENCES method_metric(method_id),
    CONSTRAINT fk_call_project FOREIGN KEY (project_id) REFERENCES project(project_id)
);

-- Bulk Import Example (MySQL)
/*
LOAD DATA INFILE 'project.csv' INTO TABLE project FIELDS TERMINATED BY ',' ENCLOSED BY '"' IGNORE 1 LINES;
LOAD DATA INFILE 'file_metric.csv' INTO TABLE file_metric FIELDS TERMINATED BY ',' ENCLOSED BY '"' IGNORE 1 LINES;
LOAD DATA INFILE 'class_metric.csv' INTO TABLE class_metric FIELDS TERMINATED BY ',' ENCLOSED BY '"' IGNORE 1 LINES;
LOAD DATA INFILE 'method_metric.csv' INTO TABLE method_metric FIELDS TERMINATED BY ',' ENCLOSED BY '"' IGNORE 1 LINES;
LOAD DATA INFILE 'call_graph.csv' INTO TABLE call_graph FIELDS TERMINATED BY ',' ENCLOSED BY '"' IGNORE 1 LINES;
*/

-- Bulk Import Example (PostgreSQL)
/*
COPY project FROM 'project.csv' WITH (FORMAT csv, HEADER true, QUOTE '"');
COPY file_metric FROM 'file_metric.csv' WITH (FORMAT csv, HEADER true, QUOTE '"');
COPY class_metric FROM 'class_metric.csv' WITH (FORMAT csv, HEADER true, QUOTE '"');
COPY method_metric FROM 'method_metric.csv' WITH (FORMAT csv, HEADER true, QUOTE '"');
COPY call_graph FROM 'call_graph.csv' WITH (FORMAT csv, HEADER true, QUOTE '"');
*/
