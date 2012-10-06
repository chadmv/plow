

CREATE SCHEMA plow;

/**
 * Projects
 */
CREATE TABLE plow.project (
  pk_project UUID NOT NULL PRIMARY KEY,
  pk_folder_default UUID,
  str_name VARCHAR(8) NOT NULL,
  str_title VARCHAR(255) NOT NULL
) WITHOUT OIDS;

/**
 * Folders
 */
CREATE TABLE plow.folder (
  pk_folder UUID NOT NULL PRIMARY KEY,
  pk_parent UUID,
  pk_project UUID NOT NULL,
  str_name VARCHAR(96) NOT NULL
) WITHOUT OIDS;


CREATE TABLE plow.folder_dsp (
  pk_folder UUID NOT NULL PRIMARY KEY,
  int_max_cores INTEGER NOT NULL DEFAULT -1,
  int_min_cores INTEGER NOT NULL DEFAULT 0,
  int_run_cores INTEGER NOT NULL DEFAULT 0
) WITHOUT OIDS;

/**
 * Jobs
 */
CREATE TABLE plow.job (
  pk_job UUID NOT NULL PRIMARY KEY,
  pk_project UUID NOT NULL,
  pk_folder UUID,
  str_name VARCHAR(160) NOT NULL,
  str_active_name VARCHAR(160),
  int_uid INTEGER NOT NULL,
  int_state SMALLINT NOT NULL DEFAULT 0,
  bool_paused BOOLEAN NOT NULL DEFAULT 'f',
  time_started BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW()),
  time_stopped BIGINT DEFAULT 0
) WITHOUT OIDS;

CREATE TABLE plow.job_dsp (
  pk_job UUID NOT NULL PRIMARY KEY,
  int_max_cores INTEGER NOT NULL DEFAULT -1,
  int_min_cores INTEGER NOT NULL DEFAULT 0,
  int_run_cores INTEGER NOT NULL DEFAULT 0
);

/**
 * Layers
 */
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


CREATE INDEX layer_str_tags_gin_idx ON plow.layer USING gin(str_tags);

/**
 * Tasks
 */
CREATE TABLE plow.task (
  pk_task UUID NOT NULL PRIMARY KEY,
  pk_layer UUID NOT NULL,
  str_name VARCHAR(255),
  int_number INTEGER NOT NULL,
  int_state SMALLINT NOT NULL,
  int_depend_count INTEGER NOT NULL DEFAULT 0,
  int_task_order INTEGER NOT NULL,
  bool_reserved BOOLEAN DEFAULT 'f' NOT NULL
) WITHOUT OIDS;


/*
 * -----------------------------------------------------
 * Stats - Keeps a count of the frame status types.
 * -----------------------------------------------------
 **/

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

/*-----------------------------------------------------*/

CREATE TABLE plow.cluster (
  pk_cluster UUID NOT NULL PRIMARY KEY,
  str_name VARCHAR(128) NOT NULL,
  str_tag VARCHAR(32) NOT NULL,
  bool_locked BOOLEAN DEFAULT 'f' NOT NULL
) WITHOUT OIDS;

INSERT INTO plow.cluster VALUES ('00000000-0000-0000-0000-000000000000', 'unassigned', 'unassigned', 'f');

/** Tag and name are unique **/

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

/**
 * Contains actual hardware status.
 */
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

CREATE TABLE plow.node_dsp (
  pk_node UUID NOT NULL PRIMARY KEY,
  int_cores SMALLINT NOT NULL,
  int_memory INTEGER NOT NULL,
  int_free_cores SMALLINT NOT NULL,
  int_free_memory INTEGER NOT NULL
) WITHOUT OIDS;

CREATE TABLE plow.quota (
  pk_quota UUID NOT NULL PRIMARY KEY,
  pk_cluster UUID NOT NULL,
  pk_project UUID NOT NULL,
  int_size INTEGER NOT NULL,
  int_burst INTEGER NOT NULL,
  int_run_cores INTEGER DEFAULT 0 NOT NULL,
  bool_locked BOOLEAN DEFAULT 'f' NOT NULL
) WITHOUT OIDS;

CREATE TABLE plow.proc (
  pk_proc UUID NOT NULL PRIMARY KEY,
  pk_quota UUID NOT NULL,
  pk_node UUID NOT NULL,
  pk_task UUID NOT NULL,
  int_cores SMALLINT NOT NULL,
  int_mem INTEGER NOT NULL,
  int_mem_used INTEGER DEFAULT 0 NOT NULL,
  int_mem_high INTEGER DEFAULT 0 NOT NULL,
  bool_unbooked BOOLEAN DEFAULT 'f' NOT NULL
) WITHOUT OIDS;




/*-----------------------------------------------------*/



/*-----------------------------------------------------*/

/**
 *
 */
CREATE OR REPLACE FUNCTION plow.before_task_depend_check() RETURNS TRIGGER AS $$
BEGIN
  NEW.int_state := 5;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_before_task_depend_check BEFORE UPDATE ON plow.task
    FOR EACH ROW WHEN (NEW.int_depend_count > 0 AND NEW.int_state=1)
    EXECUTE PROCEDURE plow.before_task_depend_check();


/**
 * plow.after_proc_created()
 *
 *
 */
CREATE OR REPLACE FUNCTION plow.after_proc_created() RETURNS TRIGGER AS $$
BEGIN

  /**
   * Update node_dsp
   **/
  UPDATE plow.node_dsp SET
    int_free_cores = int_free_cores - new.int_cores,
    int_free_memory = int_free_memory - new.int_memory
  WHERE
    pk_node = new.pk_node;

  /**
   * Update quota dsp
   **/
  UPDATE plow.quota_dsp SET
    int_cores = int_cores + new.int_cores
  WHERE
    pk_quota = new.pk_quota;

  RETURN NEW;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_after_proc_created AFTER INSERT ON plow.proc
    FOR EACH ROW EXECUTE PROCEDURE plow.after_proc_created();

/**
 * plow.after_proc_updated()
 *
 *
 */
CREATE OR REPLACE FUNCTION plow.after_proc_updated() RETURNS TRIGGER AS $$
BEGIN

  /**
   * Node DSP has to be updated with the differences.
   **/
  UPDATE plow.node_dsp SET
    int_free_cores = int_free_cores + (new.int_cores - old.int_cores),
    int_free_memory = int_free_memory + (new.int_memory - old.int_memory)
  WHERE
    pk_node = new.pk_node;

  /**
   * Update quota dsp
   **/
  UPDATE plow.quota_dsp SET
    int_cores = int_cores + (new.int_cores - old.int_cores)
  WHERE
    pk_quota = new.pk_quota;

  RETURN NEW;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_after_proc_updated AFTER UPDATE ON plow.proc
    FOR EACH ROW WHEN (OLD.pk_task != NEW.pk_task)
    EXECUTE PROCEDURE plow.after_proc_updated();

/**
 * plow.after_proc_destroyed()
 *
 *
 */
CREATE OR REPLACE FUNCTION plow.after_proc_destroyed() RETURNS TRIGGER AS $$
BEGIN

  /**
   * Update node_dsp
   **/
  UPDATE plow.node_dsp SET
    int_cores = int_cores - new.int_cores,
    int_memory = int_memory - new.int_memory,
    int_free_cores = int_free_cores + new.int_cores,
    int_free_memory = int_free_memory + new.int_memory
  WHERE
    pk_node = new.pk_node;

  /**
   * Update quota dsp
   **/
  UPDATE plow.quota_dsp SET
    int_cores = int_cores - new.int_cores
  WHERE
    pk_quota = new.pk_quota;

  RETURN NEW;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_after_proc_destroyed AFTER DELETE ON plow.proc
    FOR EACH ROW EXECUTE PROCEDURE plow.after_proc_destroyed();


/**
 * plow.after_task_state_change()
 *
 * Handle incrementing and decrementing the frame state counters
 * when a frame changes its state. Does not execute during job
 * initialization, so its up to the application server to set
 * the initial counts.
 */
CREATE OR REPLACE FUNCTION plow.after_task_state_change() RETURNS TRIGGER AS $$
DECLARE
    old_state_col VARCHAR;
    new_state_col VARCHAR;
    states VARCHAR[];
BEGIN

  states := ARRAY['INITIALIZE', 'WAITING', 'RUNNING', 'DEAD', 'EATEN' , 'DEPEND', 'SUCCEEDED'];
  old_state_col := 'int_' || lower(states[old.int_state]);
  new_state_col := 'int_' || lower(states[new.int_state]);

  EXECUTE 'UPDATE plow.layer_count SET ' || old_state_col || '=' || old_state_col || ' -1, '
            || new_state_col || '=' || new_state_col || '+1 WHERE pk_layer=$1' USING new.pk_layer;

  EXECUTE 'UPDATE plow.job_count SET ' || old_state_col || '=' || old_state_col || ' -1, '
            || new_state_col || '=' || new_state_col || '+1 WHERE pk_job='
            || '(SELECT pk_job FROM plow.layer WHERE pk_layer=$1)' USING new.pk_layer;

  RETURN NEW;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_after_task_state_change AFTER UPDATE ON plow.task
    FOR EACH ROW WHEN (OLD.int_state != 0 AND OLD.int_state != NEW.int_state)
    EXECUTE PROCEDURE plow.after_task_state_change();



