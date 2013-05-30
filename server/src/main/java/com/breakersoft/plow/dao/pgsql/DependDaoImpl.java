package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Depend;
import com.breakersoft.plow.DependE;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.DependDao;
import com.breakersoft.plow.thrift.DependType;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class DependDaoImpl extends AbstractDao implements DependDao {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(DependDaoImpl.class);

    public static final RowMapper<Depend> MAPPER = new RowMapper<Depend>() {
        @Override
        public Depend mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            DependE depend = new DependE();
            depend.setDependId((UUID) rs.getObject("pk_depend"));
            depend.setActive(rs.getBoolean("bool_active"));
            depend.setType(DependType.findByValue(rs.getInt("int_type")));
            depend.setDependentJobId((UUID) rs.getObject("pk_dependent_job"));
            depend.setDependOnJobId((UUID) rs.getObject("pk_dependon_job"));

            if (depend.getType().equals(DependType.JOB_ON_JOB)) {
                return depend;
            }

            depend.setDependentLayerId((UUID) rs.getObject("pk_dependent_layer"));
            depend.setDependOnLayerId((UUID) rs.getObject("pk_dependon_layer"));

            switch (depend.getType()) {

                case LAYER_ON_TASK:
                    depend.setDependOnTaskId((UUID) rs.getObject("pk_dependon_task"));
                    break;
                case TASK_ON_LAYER:
                    depend.setDependentTaskId((UUID) rs.getObject("pk_dependent_task"));
                    break;
                case TASK_ON_TASK:
                    depend.setDependentTaskId((UUID) rs.getObject("pk_dependent_task"));
                    depend.setDependOnTaskId((UUID) rs.getObject("pk_dependon_task"));
                    break;
            }

            return depend;
        }
    };

    private static final String GET =
            "SELECT " +
                "depend.* " +
            "FROM " +
                "depend " +
            "WHERE " +
                "pk_depend=?";

    @Override
    public Depend get(UUID id) {
        return jdbc.queryForObject(GET, MAPPER, id);
    }

    private static final String BASE_INC =
            "UPDATE plow.task SET int_depend_count=int_depend_count +1 WHERE ";

    @Override
    public void incrementDependCounts(Depend depend) {

        int result = 0;
        switch (depend.getType()) {

            case JOB_ON_JOB:
                result = jdbc.update(BASE_INC + "task.pk_job=?", depend.getDependentJobId());
                break;
            case LAYER_ON_LAYER:
            case LAYER_ON_TASK:
                result = jdbc.update(BASE_INC + "task.pk_layer=?", depend.getDependentLayerId());
                break;
            case TASK_ON_LAYER:
            case TASK_ON_TASK:
                result = jdbc.update(BASE_INC + "task.pk_task=?", depend.getDependentTaskId());
                break;
        }
        logger.info("Incremented {} depend counts {}", depend.getType(), result);
    }

    private static final String BASE_DEC =
            "UPDATE plow.task SET int_depend_count=int_depend_count -1 WHERE ";

    public void decrementDependCounts(Depend depend) {

        int result = 0;
        switch (depend.getType()) {

            case JOB_ON_JOB:
                result = jdbc.update(BASE_DEC + "task.pk_job=?", depend.getDependentJobId());
                break;
            case LAYER_ON_LAYER:
            case LAYER_ON_TASK:
                result = jdbc.update(BASE_DEC + "task.pk_layer=?", depend.getDependentLayerId());
                break;
            case TASK_ON_LAYER:
            case TASK_ON_TASK:
                result = jdbc.update(BASE_DEC + "task.pk_task=?", depend.getDependentTaskId());
                break;
        }

        logger.info("Incremented depend counts {}", depend.getType(), result);
    }

    private static final String GET_BY_TASK =
            "SELECT " +
                "depend.* " +
            "FROM " +
                "depend " +
            "WHERE " +
                "bool_active='t' " +
            "AND " +
                "int_type IN (?,?) " +
            "AND " +
                "pk_dependon_task=? " +
            "ORDER BY " +
                "depend.pk_depend ";

    @Override
    public List<Depend> getOnTaskDepends(Task task) {
        return jdbc.query(GET_BY_TASK, MAPPER,
                DependType.LAYER_ON_TASK.ordinal(),
                DependType.TASK_ON_TASK.ordinal(),
                task.getTaskId());
    }

    private static final String GET_BY_LAYER =
            "SELECT " +
                "depend.* " +
            "FROM " +
                "depend " +
            "WHERE " +
                "bool_active='t' " +
            "AND " +
                "int_type IN (?,?) " +
            "AND " +
                "pk_dependon_layer=? " +
            "ORDER BY " +
                "depend.pk_depend ";

    @Override
    public List<Depend> getOnLayerDepends(Layer layer) {
        return jdbc.query(GET_BY_LAYER, MAPPER,
                DependType.LAYER_ON_LAYER.ordinal(),
                DependType.TASK_ON_LAYER.ordinal(),
                layer.getLayerId());
    }

    private static final String GET_BY_JOB =
            "SELECT " +
                "depend.* " +
            "FROM " +
                "depend " +
            "WHERE " +
                "bool_active='t' " +
            "AND " +
                "int_type = ? " +
            "AND " +
                "pk_dependon_job=? " +
            "ORDER BY " +
                "depend.pk_depend ";

    @Override
    public List<Depend> getOnJobDepends(Job job) {
        return jdbc.query(GET_BY_JOB, MAPPER,
                DependType.JOB_ON_JOB.ordinal(),
                job.getJobId());
    }

    private static final String SATISFY =
            "UPDATE depend SET uuid_sig=NULL, bool_active='f', time_satisfied=plow.txTimeMillis() WHERE pk_depend=? AND bool_active='t'";

    @Override
    public boolean satisfyDepend(Depend depend) {
        return jdbc.update(SATISFY, depend.getDependId()) == 1;
    }

    private static final String UNSATISFY =
            "UPDATE depend SET uuid_sig=?, bool_active='t', time_satisfied=plow.txTimeMillis() WHERE pk_depend=? AND bool_active='f'";

    @Override
    public boolean unsatisfyDepend(Depend depend) {
        return jdbc.update(UNSATISFY, depend.genSig(), depend.getDependId()) == 1;
    }

    private static final String INSERT =
            JdbcUtils.Insert(
                    "plow.depend",
                    "pk_depend",
                    "uuid_sig",
                    "int_type",
                    "pk_dependent_job",
                    "pk_dependon_job",
                    "pk_dependent_layer",
                    "pk_dependon_layer",
                    "pk_dependent_task",
                    "pk_dependon_task",
                    "str_dependent_job_name",
                    "str_dependon_job_name",
                    "str_dependent_layer_name",
                    "str_dependon_layer_name",
                    "str_dependent_task_name",
                    "str_dependon_task_name");
    @Override
    public Depend createJobOnJob(Job dependent, Job dependOn) {

        final DependType type = DependType.JOB_ON_JOB;
        final UUID id = UUID.randomUUID();
        final DependE result = new DependE();
        result.setActive(true);
        result.setType(type);
        result.setDependId(id);
        result.setDependentJobId(dependent.getJobId());
        result.setDependOnJobId(dependOn.getJobId());

        logger.info("Setting up Job on Job depend: {} -> {}", dependent, dependOn);

        jdbc.update(INSERT,
                id,
                result.genSig(),
                type.ordinal(),
                dependent.getJobId(),
                dependOn.getJobId(),
                null, null, null, null,
                dependent.getName(),
                dependOn.getName(),
                null, null, null, null);

        return result;
    }

    @Override
    public Depend createLayerOnLayer(Job dependentJob, Layer dependent, Job dependOnJob, Layer dependOn) {

        final DependType type = DependType.LAYER_ON_LAYER;
        final UUID id = UUID.randomUUID();
        final DependE result = new DependE();
        result.setActive(true);
        result.setType(type);
        result.setDependId(id);
        result.setDependentJobId(dependent.getJobId());
        result.setDependOnJobId(dependOn.getJobId());
        result.setDependentLayerId(dependent.getLayerId());
        result.setDependOnLayerId(dependOn.getLayerId());

        logger.info("Setting up Layer on Layer depend: {} -> {}", dependent, dependOn);

        jdbc.update(INSERT,
                id,
                result.genSig(),
                type.ordinal(),
                dependent.getJobId(),
                dependOn.getJobId(),
                dependent.getLayerId(),
                dependOn.getLayerId(),
                null, null,
                dependentJob.getName(),
                dependOnJob.getName(),
                dependent.getName(),
                dependOn.getName(),
                null, null);


        return result;
    }

    @Override
    public Depend createLayerOnTask(
            Job dependentJob,
            Layer dependent,
            Job dependOnJob,
            Layer dependOnLayer,
            Task dependOn) {

        final DependType type = DependType.LAYER_ON_TASK;
        final UUID id = UUID.randomUUID();
        final DependE result = new DependE();
        result.setActive(true);
        result.setType(type);
        result.setDependId(id);
        result.setDependentJobId(dependent.getJobId());
        result.setDependOnJobId(dependOn.getJobId());
        result.setDependentLayerId(dependent.getLayerId());
        result.setDependOnLayerId(dependOn.getLayerId());
        result.setDependOnTaskId(dependOn.getTaskId());

        logger.info("Setting up Layer on Task depend: {} -> {}", dependent, dependOn);

        jdbc.update(INSERT,
                id,
                result.genSig(),
                type.ordinal(),
                dependent.getJobId(),
                dependOn.getJobId(),
                dependent.getLayerId(),
                dependOn.getLayerId(),
                null,
                dependOn.getTaskId(),
                dependentJob.getName(),
                dependent.getName(),
                null,
                dependOnJob.getName(),
                dependOnLayer.getName(),
                dependOn.getName());


        return result;

    }

    @Override
    public Depend createTaskOnLayer(
            Job dependentJob,
            Layer dependentLayer,
            Task dependentTask,
            Job dependOnJob,
            Layer dependOnLayer) {

        final DependType type = DependType.TASK_ON_LAYER;
        final UUID id = UUID.randomUUID();
        final DependE result = new DependE();
        result.setActive(true);
        result.setType(type);
        result.setDependId(id);
        result.setDependentJobId(dependentJob.getJobId());
        result.setDependOnJobId(dependOnJob.getJobId());
        result.setDependentLayerId(dependentLayer.getLayerId());
        result.setDependOnLayerId(dependOnLayer.getLayerId());
        result.setDependentTaskId(dependentTask.getTaskId());

        logger.info("Setting up Task on Layer depend: {} -> {}", dependentTask, dependOnLayer);

        jdbc.update(INSERT,
                id,
                result.genSig(),
                type.ordinal(),
                dependentTask.getJobId(),
                dependOnJob.getJobId(),
                dependentTask.getLayerId(),
                dependOnLayer.getLayerId(),
                dependentTask.getTaskId(),
                null,
                dependentJob.getName(),
                dependOnJob.getName(),
                dependentLayer.getName(),
                dependOnLayer.getName(),
                dependentTask.getName(),
                null);

        return result;
    }

    @Override
    public Depend createTaskOnTask(
            Job dependentJob,
            Layer dependentLayer,
            Task dependentTask,
            Job dependOnJob,
            Layer dependOnLayer,
            Task dependOnTask) {

        final DependType type = DependType.TASK_ON_TASK;
        final UUID id = UUID.randomUUID();
        final DependE result = new DependE();
        result.setActive(true);
        result.setType(type);
        result.setDependId(id);
        result.setDependentJobId(dependentTask.getJobId());
        result.setDependOnJobId(dependOnTask.getJobId());
        result.setDependentLayerId(dependentTask.getLayerId());
        result.setDependOnLayerId(dependOnTask.getLayerId());
        result.setDependentTaskId(dependentTask.getTaskId());
        result.setDependOnTaskId(dependOnTask.getTaskId());

        logger.info("Setting up Task on Task depend: {} -> {}", dependentTask, dependOnTask);

        jdbc.update(INSERT,
                id,
                result.genSig(),
                type.ordinal(),
                dependentTask.getJobId(),
                dependOnTask.getJobId(),
                dependentTask.getLayerId(),
                dependOnTask.getLayerId(),
                dependentTask.getTaskId(),
                dependOnTask.getTaskId(),
                dependentJob.getName(),
                dependOnJob.getName(),
                dependentLayer.getName(),
                dependOnLayer.getName(),
                dependentTask.getName(),
                dependOnTask.getName());

        return result;
    }
}
