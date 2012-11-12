
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
CREATE OR REPLACE FUNCTION plow.before_update_set_depend RETURNS TRIGGER AS $$
BEGIN
  NEW.int_state := 5;
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
CREATE OR REPLACE FUNCTION plow.before_update_set_waiting RETURNS TRIGGER AS $$
BEGIN
  NEW.int_state := 1;
  RETURN NEW;
END
$$
LANGUAGE plpgsql;

CREATE TRIGGER trig_before_update_set_waiting BEFORE UPDATE ON plow.task
    FOR EACH ROW WHEN (NEW.int_depend_count=0 AND NEW.int_state=5)
    EXECUTE PROCEDURE plow.before_update_set_waiting();
