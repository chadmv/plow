
/**
 * Projects
 */
CREATE TABLE plow.project (
  pk_project UUID NOT NULL PRIMARY KEY,
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

/**
 * Jobs
 */
CREATE TABLE plow.job (
  pk_job UUID NOT NULL PRIMARY KEY,
  pk_project UUID NOT NULL,
  pk_folder UUID,
  str_name VARCHAR(160) NOT NULL,
  str_active_name VARCHAR(160),
  str_user VARCHAR(128) NOT NULL,
  int_uid INTEGER NOT NULL,
  int_state SMALLINT NOT NULL DEFAULT 0,
  bool_paused BOOLEAN NOT NULL DEFAULT 'f',
  time_started BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW()),
  time_stopped BIGINT DEFAULT 0
) WITHOUT OIDS;

/**
 * Layers
 */
CREATE table plow.layer (
  pk_layer UUID NOT NULL PRIMARY KEY,
  pk_job UUID NOT NULL,
  str_name VARCHAR(200) NOT NULL,
  str_range TEXT NOT NULL,
  str_command TEXT NOT NULL,
  int_chunk_size INTEGER NOT NULL,
  int_order INTEGER NOT NULL,
  int_min_cores SMALLINT NOT NULL,
  int_max_cores SMALLINT NOT NULL,
  int_min_mem INTEGER NOT NULL
) WITHOUT OIDS;

/**
 * Frames
 */
CREATE TABLE plow.frame (
  pk_frame UUID NOT NULL PRIMARY KEY,
  pk_layer UUID NOT NULL,
  str_alias VARCHAR(255),
  int_number INTEGER NOT NULL,
  int_order INTEGER NOT NULL,
  int_state SMALLINT NOT NULL,
  int_depend_count INTEGER NOT NULL DEFAULT 0
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
  str_name VARCHAR(128) NOT NULL
) WITHOUT OIDS;

CREATE TABLE plow.node (
  pk_node UUID NOT NULL PRIMARY KEY,
  pk_cluster UUID NOT NULL,
  str_name VARCHAR(160) NOT NULL,

) WITHOUT OIDS;

/**
 *
 */
CREATE TABLE plow.node_dsp (
  pk_node_dsp UUID NOT NULL PRIMARY KEY,
  pk_node UUID NOT NULL,
  int_total_cores SMALLINT NOT NULL,
  int_total_mem INTEGER NOT NULL,
  int_idle_cores SMALLINT NOT NULL,
  int_idle_mem INTEGER NOT NULL
) WITHOUT OIDS;



CREATE TABLE plow.sub (
  pk_sub UUID NOT NULL PRIMARY KEY,
  pk_cluster UUID NOT NULL,
  pk_project UUID NOT NULL,
  int_cores SMALLINT NOT NULL DEFAULT 0,
  int_burst INTEGER NOT NULL DEFAULT 0
) WITHOUT OIDS;

/*-----------------------------------------------------*/

CREATE TABLE plow.proc (
  pk_proc UUID NOT NULL PRIMARY KEY,
  pk_host UUID NOT NULL,
  pk_frame NOT NULL,
  int_cores SMALLINT NOT NULL,
  int_cores_used SMALLINT NOT NULL,
  int_mem INTEGER NOT NULL,
  int_mem_used INTEGER NOT NULL,
  int_mem_high INTEGER NOT NULL,
  bool_unbooked NOT NULL BOOLEAN DEFAULT 'f'
) WITHOUT OIDS;


/*-----------------------------------------------------*/

/**
 *
 */
CREATE OR REPLACE FUNCTION plow.before_frame_depend_check() RETURNS TRIGGER AS $$
BEGIN
  NEW.int_state := 5;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_before_frame_depend_check BEFORE UPDATE ON plow.frame
    FOR EACH ROW WHEN (NEW.int_depend_count > 0 AND NEW.int_state=1)
    EXECUTE PROCEDURE plow.before_frame_depend_check();

/**
 * plow.after_frame_state_change()
 *
 * Handle incrementing and decrementing the frame state counters
 * when a frame changes its state. Does not execute during job
 * initialization, so its up to the application server to set
 * the initial counts.
 */
CREATE OR REPLACE FUNCTION plow.after_frame_state_change() RETURNS TRIGGER AS $$
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

CREATE TRIGGER trig_after_frame_state_change AFTER UPDATE ON plow.frame
    FOR EACH ROW WHEN (OLD.int_state != 0 AND OLD.int_state != NEW.int_state)
    EXECUTE PROCEDURE plow.after_frame_state_change();



