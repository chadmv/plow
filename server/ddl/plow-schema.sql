/**
 *
 * Table suffixes.
 * No suffix - state/configuration data.
 * _ping - data that is updated via RnDaemon pings
 * _dsp - data that is updated via dispatch
 * _count - counts maintained by triggers
 *
 */
CREATE SCHEMA plow;
CREATE LANGUAGE plpgsql;

---
--- Return the current clock time in millis.
---
CREATE OR REPLACE FUNCTION plow.currentTimeMillis() RETURNS BIGINT AS $$
BEGIN
    return (EXTRACT(EPOCH FROM clock_timestamp()) * 1000)::bigint;
END;
$$ LANGUAGE plpgsql;


---
--- Returns the current time in millis, doesn't change during a transaction.
---
CREATE OR REPLACE FUNCTION plow.txTimeMillis() RETURNS BIGINT AS $$
BEGIN
    return (EXTRACT(EPOCH FROM NOW()) * 1000)::bigint;
END;
$$ LANGUAGE plpgsql;


----------------------------------------------------------

---
--- Project
---

CREATE TABLE plow.project (
  pk_project UUID NOT NULL PRIMARY KEY,
  pk_folder_default UUID,
  str_name VARCHAR(16) NOT NULL,
  str_title VARCHAR(255) NOT NULL
) WITHOUT OIDS;

----------------------------------------------------------

---
--- Folders
---

CREATE TABLE plow.folder (
  pk_folder UUID NOT NULL PRIMARY KEY,
  pk_project UUID NOT NULL,
  str_name VARCHAR(128) NOT NULL,
  int_order SMALLINT NOT NULL,
  time_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
) WITHOUT OIDS;


CREATE UNIQUE INDEX folder_name_pk_project_uniq_idx ON plow.folder (str_name, pk_project);
CREATE INDEX folder_pk_project_idx ON plow.folder (pk_project);


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
  str_log_path TEXT,
  int_uid INTEGER NOT NULL,
  int_state SMALLINT NOT NULL DEFAULT 0,
  bool_paused BOOLEAN NOT NULL DEFAULT 'f',
  time_started BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW()),
  time_stopped BIGINT DEFAULT 0
) WITHOUT OIDS;

CREATE UNIQUE INDEX job_str_active_name_uniq_idx ON plow.job (str_active_name);
CREATE INDEX job_int_state_pk_project_idx ON plow.job (int_state, pk_project);
CREATE INDEX job_pk_project_idx ON plow.job (pk_project);

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

---

CREATE TABLE plow.job_ping (
  pk_job UUID NOT NULL PRIMARY KEY,
  int_max_rss INTEGER NOT NULL DEFAULT 0
);

----------------------------------------------------------

---
--- Layers
---

CREATE table plow.layer (
  pk_layer UUID NOT NULL PRIMARY KEY,
  pk_job UUID NOT NULL,
  str_name VARCHAR(200) NOT NULL,
  str_range TEXT,
  str_command TEXT[] NOT NULL,
  str_tags TEXT[] NOT NULL,
  int_chunk_size INTEGER NOT NULL,
  int_order INTEGER NOT NULL,
  int_min_cores SMALLINT NOT NULL,
  int_max_cores SMALLINT NOT NULL,
  int_min_ram INTEGER NOT NULL
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

---

CREATE TABLE plow.layer_dsp (
  pk_layer UUID NOT NULL PRIMARY KEY,
  int_run_cores INTEGER NOT NULL DEFAULT 0
);


CREATE TABLE plow.layer_ping (
  pk_layer UUID NOT NULL PRIMARY KEY,
  int_max_rss INTEGER NOT NULL DEFAULT 0,
  int_max_cpu_perc SMALLINT NOT NULL DEFAULT 0
);

---

CREATE TABLE plow.output (
    pk_output UUID NOT NULL PRIMARY KEY,
    pk_layer UUID NOT NULL,
    pk_job UUID NOT NULL,
    str_path TEXT NOT NULL,
    attrs hstore
);

CREATE INDEX output_pk_layer_idx ON plow.output (pk_layer);
CREATE INDEX output_pk_job_idx ON plow.output (pk_job);

----------------------------------------------------------

---
--- Tasks
---

CREATE TABLE plow.task (
  pk_task UUID NOT NULL PRIMARY KEY,
  pk_layer UUID NOT NULL,
  pk_job UUID NOT NULL,
  str_name VARCHAR(255),
  int_number INTEGER NOT NULL,
  int_state SMALLINT NOT NULL,
  int_depend_count INTEGER NOT NULL DEFAULT 0,
  int_task_order INTEGER NOT NULL,
  int_layer_order INTEGER NOT NULL,
  bool_reserved BOOLEAN DEFAULT 'f' NOT NULL,
  time_started BIGINT DEFAULT 0 NOT NULL,
  time_stopped BIGINT DEFAULT 0 NOT NULL,
  time_updated BIGINT DEFAULT 0 NOT NULL,
  int_retry SMALLINT DEFAULT -1 NOT NULL,
  int_cores SMALLINT DEFAULT 0 NOT NULL,
  int_ram INTEGER DEFAULT 0 NOT NULL
) WITHOUT OIDS;

CREATE INDEX task_pk_layer_idx ON plow.task (pk_layer);
CREATE INDEX task_pk_job_idx ON plow.task (pk_job);
CREATE INDEX task_int_state_idx ON plow.task (int_state);
CREATE INDEX task_time_updated_idx ON plow.task (time_updated);
CREATE UNIQUE INDEX task_str_name_pk_job_idx_uniq ON plow.task (str_name, pk_job);

CREATE INDEX task_order_idx ON plow.task(int_task_order, int_layer_order);

----------------------------------------------------------


---
--- Stores the ping data for a task.
---
CREATE TABLE task_ping (
  pk_task UUID NOT NULL PRIMARY KEY,
  int_rss INTEGER DEFAULT 0 NOT NULL,
  int_max_rss INTEGER DEFAULT 0 NOT NULL,
  int_cpu_perc SMALLINT NOT NULL DEFAULT 0,
  int_max_cpu_perc SMALLINT NOT NULL DEFAULT 0,
  int_progress SMALLINT DEFAULT 0 NOT NULL,
  str_last_log_line TEXT,
  str_last_node_name TEXT
) WITHOUT OIDS;

---
--- Dependencies
---

CREATE TABLE plow.depend (
    pk_depend UUID NOT NULL PRIMARY KEY,
    uuid_sig UUID,
    int_type SMALLINT NOT NULL,
    bool_active BOOLEAN NOT NULL DEFAULT 't',
    pk_dependent_job UUID NOT NULL,
    pk_dependon_job UUID NOT NULL,
    pk_dependent_layer UUID,
    pk_dependon_layer UUID,
    pk_dependent_task UUID,
    pk_dependon_task UUID,
    time_created BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW()),
    time_modified BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())
);

CREATE UNIQUE INDEX depend_uuid_sig_idx ON plow.depend (uuid_sig);

CREATE INDEX depend_dependent_job_idx ON plow.depend (pk_dependent_job);
CREATE INDEX depend_dependon_job_idx ON plow.depend (pk_dependon_job);
CREATE INDEX depend_dependent_layer_idx ON plow.depend (pk_dependent_layer);
CREATE INDEX depend_dependon_layer_idx ON plow.depend (pk_dependon_layer);
CREATE INDEX depend_dependent_task_idx ON plow.depend (pk_dependent_task);
CREATE INDEX depend_dependon_task_idx ON plow.depend (pk_dependon_task);

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
  time_created BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW()),
  time_updated BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW()),
  str_tags TEXT[] NOT NULL
) WITHOUT OIDS;

CREATE UNIQUE INDEX node_str_name_uniq_idx ON plow.node (str_name);
CREATE INDEX node_int_state_idx ON plow.node (int_state);

---

CREATE TABLE plow.node_sys (
  pk_node UUID NOT NULL PRIMARY KEY,
  int_phys_cores SMALLINT NOT NULL,
  int_log_cores SMALLINT NOT NULL,
  int_ram INTEGER NOT NULL,
  int_free_ram INTEGER NOT NULL,
  int_swap INTEGER NOT NULL,
  int_free_swap INTEGER NOT NULL,
  time_booted BIGINT NOT NULL,
  str_cpu_model TEXT NOT NULL,
  str_platform TEXT NOT NULL
) WITHOUT OIDS;

---

CREATE TABLE plow.node_dsp (
  pk_node UUID NOT NULL PRIMARY KEY,
  int_cores SMALLINT NOT NULL,
  int_ram INTEGER NOT NULL,
  int_idle_cores SMALLINT NOT NULL,
  int_free_ram INTEGER NOT NULL
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
  pk_node UUID NOT NULL,
  pk_job UUID NOT NULL,
  pk_task UUID,
  int_cores SMALLINT NOT NULL,
  int_ram INTEGER NOT NULL,
  bool_unbooked BOOLEAN DEFAULT 'f' NOT NULL,
  bool_backfill BOOLEAN DEFAULT 'f' NOT NULL
) WITHOUT OIDS;

CREATE INDEX proc_pk_node_idx ON plow.proc (pk_node);
CREATE UNIQUE INDEX proc_pk_task_uniq_idx ON plow.proc (pk_task);
CREATE INDEX proc_pk_job_idx ON plow.proc (pk_job);

----------------------------------------------------------

---
--- plow.after_task_state_change()
---
--- Handle incrementing/decrementating the frame state counters.
---
CREATE OR REPLACE FUNCTION plow.after_task_state_change() RETURNS TRIGGER AS $$
DECLARE
    old_state_col VARCHAR;
    new_state_col VARCHAR;
    states VARCHAR[];
BEGIN

  states := ARRAY['INITIALIZE', 'WAITING', 'RUNNING', 'DEAD', 'EATEN' , 'DEPEND', 'SUCCEEDED'];
  old_state_col := 'int_' || lower(states[old.int_state + 1]);
  new_state_col := 'int_' || lower(states[new.int_state + 1]);

  EXECUTE 'UPDATE plow.layer_count SET ' || old_state_col || '=' || old_state_col || ' -1, '
            || new_state_col || '=' || new_state_col || '+1 WHERE pk_layer=$1' USING new.pk_layer;

  EXECUTE 'UPDATE plow.job_count SET ' || old_state_col || '=' || old_state_col || ' -1, '
            || new_state_col || '=' || new_state_col || '+1 WHERE pk_job=$1' USING new.pk_job;

  RETURN NEW;

END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_after_task_state_change AFTER UPDATE ON plow.task
    FOR EACH ROW WHEN (OLD.int_state != 0 AND OLD.int_state != NEW.int_state)
    EXECUTE PROCEDURE plow.after_task_state_change();

---
--- plow.before_task_depend_check()
---
--- Before task dependency check. Runs if the task has a depend count
--- greater than zero and flips the state to depend.
---
CREATE OR REPLACE FUNCTION plow.before_update_set_depend() RETURNS TRIGGER AS $$
BEGIN
  NEW.int_state := 5;
  NEW.time_updated := txTimeMillis();
  RETURN NEW;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_before_update_set_depend BEFORE UPDATE ON plow.task
    FOR EACH ROW WHEN (NEW.int_depend_count > 0 AND NEW.int_state=1)
    EXECUTE PROCEDURE plow.before_update_set_depend();

---
--- plow.before_update_task_depend_check()
---
--- Before task dependency check. Runs if the task has a depend count
--- greater than zero and flips the state to depend.
---
CREATE OR REPLACE FUNCTION plow.before_update_set_waiting() RETURNS TRIGGER AS $$
BEGIN
  NEW.int_state := 1;
  NEW.time_updated := txTimeMillis();
  RETURN NEW;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_before_update_set_waiting BEFORE UPDATE ON plow.task
    FOR EACH ROW WHEN (NEW.int_depend_count=0 AND NEW.int_state=5)
    EXECUTE PROCEDURE plow.before_update_set_waiting();

----------------------------------------------------------


---
--- Test Project
---
INSERT INTO plow.project VALUES ('00000000-0000-0000-0000-000000000000', null, 'test', 'The Test Project');
INSERT INTO plow.folder VALUES ('00000000-0000-0000-0000-000000000000', '00000000-0000-0000-0000-000000000000', 'The Test Project', 0);
INSERT INTO plow.folder_dsp VALUES ('00000000-0000-0000-0000-000000000000', -1, 0, 0);
UPDATE plow.project SET pk_folder_default = '00000000-0000-0000-0000-000000000000' WHERE pk_project='00000000-0000-0000-0000-000000000000';

---
--- Test Cluster
---
INSERT INTO plow.cluster VALUES ('00000000-0000-0000-0000-000000000000', 'unassigned', 'unassigned', 'f', 't');

INSERT INTO plow.quota VALUES ('00000000-0000-0000-0000-000000000000', '00000000-0000-0000-0000-000000000000','00000000-0000-0000-0000-000000000000', 10, 20, 0, 'f');


