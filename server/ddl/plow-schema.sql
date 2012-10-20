
CREATE SCHEMA plow;
CREATE LANGUAGE plpgsql;

---
--- Project
---

CREATE TABLE plow.project (
  pk_project UUID NOT NULL PRIMARY KEY,
  pk_folder_default UUID,
  str_name VARCHAR(8) NOT NULL,
  str_title VARCHAR(255) NOT NULL
) WITHOUT OIDS;

----------------------------------------------------------

---
--- Folders
---

CREATE TABLE plow.folder (
  pk_folder UUID NOT NULL PRIMARY KEY,
  pk_parent UUID,
  pk_project UUID NOT NULL,
  str_name VARCHAR(96) NOT NULL
) WITHOUT OIDS;

---

CREATE TABLE plow.folder_dsp (
  pk_folder UUID NOT NULL PRIMARY KEY,
  int_max_cores INTEGER NOT NULL DEFAULT -1,
  int_min_cores INTEGER NOT NULL DEFAULT 0,
  int_run_cores INTEGER NOT NULL DEFAULT 0
) WITHOUT OIDS;

----------------------------------------------------------

---
--- Jobs
---

CREATE TABLE plow.job (
  pk_job UUID NOT NULL PRIMARY KEY,
  pk_project UUID NOT NULL,
  pk_folder UUID,
  str_name VARCHAR(160) NOT NULL,
  str_active_name VARCHAR(160),
  str_username VARCHAR(100) NOT NULL,
  int_uid INTEGER NOT NULL,
  int_state SMALLINT NOT NULL DEFAULT 0,
  bool_paused BOOLEAN NOT NULL DEFAULT 'f',
  time_started BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW()),
  time_stopped BIGINT DEFAULT 0
) WITHOUT OIDS;

CREATE UNIQUE INDEX job_str_active_name_uniq_idx ON plow.job (str_active_name);
CREATE INDEX job_int_state_idx ON plow.job (int_state);

---

CREATE TABLE plow.job_dsp (
  pk_job UUID NOT NULL PRIMARY KEY,
  int_max_cores INTEGER NOT NULL DEFAULT -1,
  int_min_cores INTEGER NOT NULL DEFAULT 0,
  int_run_cores INTEGER NOT NULL DEFAULT 0
);

---

CREATE TABLE plow.job_count (
  pk_job UUID NOT NULL PRIMARY KEY,
  int_total INTEGER NOT NULL DEFAULT 0,
  int_succeeded INTEGER NOT NULL DEFAULT 0,
  int_running INTEGER NOT NULL DEFAULT 0,
  int_dead INTEGER NOT NULL DEFAULT 0,
  int_eaten INTEGER NOT NULL DEFAULT 0,
  int_waiting INTEGER NOT NULL DEFAULT 0,
  int_depend INTEGER NOT NULL DEFAULT 0
) WITHOUT OIDS;

----------------------------------------------------------

---
--- Layers
---

CREATE table plow.layer (
  pk_layer UUID NOT NULL PRIMARY KEY,
  pk_job UUID NOT NULL,
  str_name VARCHAR(200) NOT NULL,
  str_range TEXT NOT NULL,
  str_command TEXT[] NOT NULL,
  str_tags TEXT[] NOT NULL,
  int_chunk_size INTEGER NOT NULL,
  int_order INTEGER NOT NULL,
  int_min_cores SMALLINT NOT NULL,
  int_max_cores SMALLINT NOT NULL,
  int_min_mem INTEGER NOT NULL
) WITHOUT OIDS;

CREATE INDEX layer_pk_job_idx ON plow.layer (pk_job);
CREATE INDEX layer_str_tags_gin_idx ON plow.layer USING gin(str_tags);

CREATE UNIQUE INDEX layer_str_name_pk_job_uniq_idx ON plow.layer (str_name, pk_job);

---

CREATE TABLE plow.layer_count (
  pk_layer UUID NOT NULL PRIMARY KEY,
  int_total INTEGER NOT NULL DEFAULT 0,
  int_succeeded INTEGER NOT NULL DEFAULT 0,
  int_running INTEGER NOT NULL DEFAULT 0,
  int_dead INTEGER NOT NULL DEFAULT 0,
  int_eaten INTEGER NOT NULL DEFAULT 0,
  int_waiting INTEGER NOT NULL DEFAULT 0,
  int_depend INTEGER NOT NULL DEFAULT 0
) WITHOUT OIDS;

CREATE INDEX layer_count_int_waiting_idx ON plow.layer_count (int_waiting);

----------------------------------------------------------

---
--- Tasks
---

CREATE TABLE plow.task (
  pk_task UUID NOT NULL PRIMARY KEY,
  pk_layer UUID NOT NULL,
  str_name VARCHAR(255),
  int_number INTEGER NOT NULL,
  int_state SMALLINT NOT NULL,
  int_depend_count INTEGER NOT NULL DEFAULT 0,
  int_task_order INTEGER NOT NULL,
  bool_reserved BOOLEAN DEFAULT 'f' NOT NULL,
  int_start_time BIGINT DEFAULT 0 NOT NULL,
  int_stop_time BIGINT DEFAULT 0 NOT NULL
) WITHOUT OIDS;

CREATE INDEX task_pk_layer_idx ON plow.task (pk_layer);
CREATE INDEX task_int_state_idx ON plow.task (int_state);


----------------------------------------------------------

---
--- Cluster
---

CREATE TABLE plow.cluster (
  pk_cluster UUID NOT NULL PRIMARY KEY,
  str_name VARCHAR(128) NOT NULL,
  str_tag VARCHAR(32) NOT NULL,
  bool_locked BOOLEAN DEFAULT 'f' NOT NULL,
  bool_default BOOLEAN DEFAULT 'f' NOT NULL
) WITHOUT OIDS;

CREATE UNIQUE INDEX cluster_str_name_uniq_idx ON plow.cluster (str_name);


----------------------------------------------------------

---
--- Node
---

CREATE TABLE plow.node (
  pk_node UUID NOT NULL PRIMARY KEY,
  pk_cluster UUID NOT NULL,
  str_name VARCHAR(128) NOT NULL,
  str_ipaddr VARCHAR(15) NOT NULL,
  int_state SMALLINT NOT NULL DEFAULT 0,
  int_lock_state SMALLINT NOT NULL DEFAULT 0,
  int_created_time BIGINT NOT NULL,
  str_tags TEXT[] NOT NULL
) WITHOUT OIDS;

CREATE UNIQUE INDEX node_str_name_uniq_idx ON plow.node (str_name);
CREATE INDEX node_int_state_idx ON plow.node (int_state);

---

CREATE TABLE plow.node_status (
  pk_node UUID NOT NULL PRIMARY KEY,
  int_phys_cores SMALLINT NOT NULL,
  int_log_cores SMALLINT NOT NULL,
  int_memory INTEGER NOT NULL,
  int_free_memory INTEGER NOT NULL,
  int_swap INTEGER NOT NULL,
  int_free_swap INTEGER NOT NULL,
  int_boot_time BIGINT NOT NULL,
  int_ping_time BIGINT NOT NULL,
  str_proc VARCHAR(128) NOT NULL,
  str_os VARCHAR(128) NOT NULL
) WITHOUT OIDS;

---

CREATE TABLE plow.node_dsp (
  pk_node UUID NOT NULL PRIMARY KEY,
  int_cores SMALLINT NOT NULL,
  int_memory INTEGER NOT NULL,
  int_free_cores SMALLINT NOT NULL,
  int_free_memory INTEGER NOT NULL
) WITHOUT OIDS;

---

CREATE TABLE plow.quota (
  pk_quota UUID NOT NULL PRIMARY KEY,
  pk_cluster UUID NOT NULL,
  pk_project UUID NOT NULL,
  int_size INTEGER NOT NULL,
  int_burst INTEGER NOT NULL,
  int_run_cores INTEGER DEFAULT 0 NOT NULL,
  bool_locked BOOLEAN DEFAULT 'f' NOT NULL
) WITHOUT OIDS;

----------------------------------------------------------

---
--- Proc
---

CREATE TABLE plow.proc (
  pk_proc UUID NOT NULL PRIMARY KEY,
  pk_quota UUID NOT NULL,
  pk_node UUID NOT NULL,
  pk_task UUID,
  int_cores SMALLINT NOT NULL,
  int_mem INTEGER NOT NULL,
  int_mem_used INTEGER DEFAULT 0 NOT NULL,
  int_mem_high INTEGER DEFAULT 0 NOT NULL,
  bool_unbooked BOOLEAN DEFAULT 'f' NOT NULL
) WITHOUT OIDS;

CREATE INDEX proc_pk_quota_idx ON plow.proc (pk_quota);
CREATE INDEX proc_pk_node_idx ON plow.proc (pk_node);
CREATE INDEX proc_pk_task_idx ON plow.proc (pk_task);
