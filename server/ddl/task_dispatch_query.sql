EXPLAIN ANALYZE
SELECT  
    task.pk_task,
    task.pk_layer,
    task.pk_job,
    task.str_name, 
    task.int_ram_min,   
    layer.int_cores_min,
    layer.int_ram_min,
    job.pk_project  
FROM  
    plow.layer  
        INNER JOIN  
    plow.task ON layer.pk_layer = task.pk_layer  
        INNER JOIN  
    plow.job ON layer.pk_job = job.pk_job  
WHERE  
    layer.pk_job = '8c46b676-ec60-496e-a671-d9012c5be32e'::uuid
AND  
    layer.int_cores_min <= 8
AND
    layer.int_ram_min <= 512
AND  
    layer.str_tags && '{unassigned}'
AND  
    task.int_state = 1
AND  
    task.bool_reserved IS FALSE  
ORDER BY  
    task.int_task_order, task.int_layer_order ASC  
LIMIT 20;