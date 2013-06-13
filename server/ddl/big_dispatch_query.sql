        EXPLAIN ANALYZE
        SELECT DISTINCT  
            job.pk_job,  
            job_dsp.float_tier AS job_tier,
            folder_dsp.float_tier AS folder_tier
        FROM  
            plow.job, 
            plow.job_dsp,  
            plow.folder_dsp,  
            plow.layer,
            plow.layer_count
        WHERE  
            job.pk_job = job_dsp.pk_job  
        AND  
            job.pk_folder = folder_dsp.pk_folder  
        AND  
            job.pk_job = layer.pk_job  
        AND  
            layer.pk_layer = layer_count.pk_layer  
        AND  
            job.pk_project = '00000000-0000-0000-0000-000000000000'::uuid
        AND  
            job.int_state  = 1
        AND  
            job.bool_paused = 'f'  
        AND  
            layer.str_tags && '{unassigned}'
        AND  
            layer.int_cores_min <= 8
        AND  
            layer.int_ram_range && int4range(0, 8096)
        AND  
            (job_dsp.int_cores_run < job_dsp.int_cores_max OR job_dsp.int_cores_max = -1)  
        AND  
            (folder_dsp.int_cores_run < folder_dsp.int_cores_max OR  folder_dsp.int_cores_max = -1)
        AND  
            layer_count.int_waiting > 0
        ORDER BY  
            job_dsp.float_tier ASC,  
            folder_dsp.float_tier ASC  
        LIMIT 100;