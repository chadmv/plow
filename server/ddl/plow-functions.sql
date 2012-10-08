
---
--- plow.clean_db()
--- Utility function for clearing the DB after testing.
---
CREATE OR REPLACE FUNCTION plow.clean_db() RETURNS VOID AS $$
BEGIN
    DELETE FROM plow.task;
    DELETE FROM plow.layer_count;
    DELETE FROM plow.layer;
    DELETE FROM plow.job_count;
    DELETE FROM plow.job_dsp;
    DELETE FROM plow.job;
END
$$
LANGUAGE plpgsql;