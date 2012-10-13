
---
--- plow.after_proc_created()
---
--- Subtract resources from proc, add them to quota.
---
CREATE OR REPLACE FUNCTION plow.after_proc_created() RETURNS TRIGGER AS $$
BEGIN

  /**
   * Update node_dsp
   **/
  UPDATE plow.node_dsp SET
    int_free_cores = int_free_cores - new.int_cores,
    int_free_memory = int_free_memory - new.int_mem
  WHERE
    pk_node = new.pk_node;

  /**
   * Update quota dsp
   **/
  UPDATE plow.quota  SET
    int_run_cores = int_run_cores + new.int_cores
  WHERE
    pk_quota = new.pk_quota;

  RETURN NEW;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_after_proc_created AFTER INSERT ON plow.proc
    FOR EACH ROW EXECUTE PROCEDURE plow.after_proc_created();

---
--- plow.after_proc_updated()
---
--- Add/subtract changes in resources to the node and quota.
---
CREATE OR REPLACE FUNCTION plow.after_proc_updated() RETURNS TRIGGER AS $$
BEGIN

  /**
   * Node DSP has to be updated with the differences.
   **/
  UPDATE plow.node_dsp SET
    int_free_cores = int_free_cores + (new.int_cores - old.int_cores),
    int_free_memory = int_free_memory + (new.int_mem - old.int_mem)
  WHERE
    pk_node = new.pk_node;

  /**
   * Update quota dsp
   **/
  UPDATE plow.quota SET
    int_run_cores = int_run_cores + (new.int_cores - old.int_cores)
  WHERE
    pk_quota = new.pk_quota;

  RETURN NEW;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_after_proc_updated AFTER UPDATE ON plow.proc
    FOR EACH ROW WHEN (OLD.pk_task != NEW.pk_task)
    EXECUTE PROCEDURE plow.after_proc_updated();

---
--- plow.after_proc_deleted()
---
--- Return resources back to node, remove from quota.
---
CREATE OR REPLACE FUNCTION plow.after_proc_deleted() RETURNS TRIGGER AS $$
BEGIN

  /**
   * Update node_dsp
   **/
  UPDATE plow.node_dsp SET
    int_free_cores = int_free_cores + old.int_cores,
    int_free_memory = int_free_memory + old.int_mem
  WHERE
    pk_node = old.pk_node;

  /**
   * Update quota dsp
   **/
  UPDATE plow.quota SET
    int_run_cores = int_run_cores - old.int_cores
  WHERE
    pk_quota = old.pk_quota;

  RETURN OLD;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_after_proc_destroyed AFTER DELETE ON plow.proc
    FOR EACH ROW EXECUTE PROCEDURE plow.after_proc_deleted();

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
            || new_state_col || '=' || new_state_col || '+1 WHERE pk_job='
            || '(SELECT pk_job FROM plow.layer WHERE pk_layer=$1)' USING new.pk_layer;

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
CREATE OR REPLACE FUNCTION plow.before_task_depend_check() RETURNS TRIGGER AS $$
BEGIN
  NEW.int_state := 5;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_before_task_depend_check BEFORE UPDATE ON plow.task
    FOR EACH ROW WHEN (NEW.int_depend_count > 0 AND NEW.int_state=1)
    EXECUTE PROCEDURE plow.before_task_depend_check();
