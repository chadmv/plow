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

    private static final String SATISFY =
            "UPDATE depend SET uuid_sig=NULL, bool_active='f' WHERE pk_depend=? AND bool_active='t'";

    @Override
    public boolean satisfyDepend(Depend depend) {
        return jdbc.update(SATISFY, depend.getDependId()) == 1;
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
                    "pk_dependon_task");
    @Override
    public Depend createJobOnJob(Job dependent, Job dependOn) {
        DependType type = DependType.JOB_ON_JOB;
        UUID id = UUID.randomUUID();
        UUID sig = genSig(
                type.toString(),
                dependent.getJobId().toString(),
                dependOn.getJobId().toString());

        jdbc.update(INSERT,
                id,
                sig,
                type.ordinal(),
                dependent.getJobId(),
                dependOn.getJobId(),
                null, null, null, null);

        DependE result = new DependE();
        result.setActive(true);
        result.setType(type);
        result.setDependId(id);
        result.setDependentJobId(dependent.getJobId());
        result.setDependOnJobId(dependOn.getJobId());
        return result;
    }

    @Override
    public Depend createLayerOnLayer(Layer dependent, Layer dependOn) {
        DependType type = DependType.LAYER_ON_LAYER;
        UUID id = UUID.randomUUID();
        UUID sig = genSig(
                type.toString(),
                dependent.getLayerId().toString(),
                dependOn.getLayerId().toString());

        jdbc.update(INSERT,
                id,
                sig,
                type.ordinal(),
                dependent.getJobId(),
                dependOn.getJobId(),
                dependent.getLayerId(),
                dependOn.getLayerId(),
                null, null);

        DependE result = new DependE();
        result.setActive(true);
        result.setType(type);
        result.setDependId(id);
        result.setDependentJobId(dependent.getJobId());
        result.setDependOnJobId(dependOn.getJobId());
        result.setDependentLayerId(dependent.getLayerId());
        result.setDependOnLayerId(dependOn.getLayerId());
        return result;
    }

    @Override
    public Depend createLayerOnTask(Layer dependent, Task dependOn) {
        DependType type = DependType.LAYER_ON_TASK;
        UUID id = UUID.randomUUID();
        UUID sig = genSig(
                type.toString(),
                dependent.getLayerId().toString(),
                dependOn.getTaskId().toString());

        jdbc.update(INSERT,
                id,
                sig,
                type.ordinal(),
                dependent.getJobId(),
                dependOn.getJobId(),
                dependent.getLayerId(),
                dependOn.getLayerId(),
                null, dependOn.getTaskId());

        DependE result = new DependE();
        result.setActive(true);
        result.setType(type);
        result.setDependId(id);
        result.setDependentJobId(dependent.getJobId());
        result.setDependOnJobId(dependOn.getJobId());
        result.setDependentLayerId(dependent.getLayerId());
        result.setDependOnLayerId(dependOn.getLayerId());
        result.setDependOnTaskId(dependOn.getTaskId());
        return result;

    }

    @Override
    public Depend createTaskOnLayer(Task dependent, Layer dependOn) {

        DependType type = DependType.TASK_ON_LAYER;
        UUID id = UUID.randomUUID();
        UUID sig = genSig(
                type.toString(),
                dependent.getTaskId().toString(),
                dependOn.getLayerId().toString());

        jdbc.update(INSERT,
                id,
                sig,
                type.ordinal(),
                dependent.getJobId(),
                dependOn.getJobId(),
                dependent.getLayerId(),
                dependOn.getLayerId(),
                dependent.getTaskId(), null);

        DependE result = new DependE();
        result.setActive(true);
        result.setType(type);
        result.setDependId(id);
        result.setDependentJobId(dependent.getJobId());
        result.setDependOnJobId(dependOn.getJobId());
        result.setDependentLayerId(dependent.getLayerId());
        result.setDependOnLayerId(dependOn.getLayerId());
        result.setDependentTaskId(dependent.getTaskId());
        return result;
    }

    @Override
    public Depend createTaskOnTask(Task dependent, Task dependOn) {

        DependType type = DependType.TASK_ON_TASK;
        UUID id = UUID.randomUUID();
        UUID sig = genSig(
                type.toString(),
                dependent.getTaskId().toString(),
                dependOn.getTaskId().toString());

        jdbc.update(INSERT,
                id,
                sig,
                type.ordinal(),
                dependent.getJobId(),
                dependOn.getJobId(),
                dependent.getLayerId(),
                dependOn.getLayerId(),
                dependent.getTaskId(),
                dependOn.getTaskId());

        DependE result = new DependE();
        result.setActive(true);
        result.setType(type);
        result.setDependId(id);
        result.setDependentJobId(dependent.getJobId());
        result.setDependOnJobId(dependOn.getJobId());
        result.setDependentLayerId(dependent.getLayerId());
        result.setDependOnLayerId(dependOn.getLayerId());
        result.setDependentTaskId(dependent.getTaskId());
        result.setDependOnTaskId(dependOn.getTaskId());
        return result;
    }

    private UUID genSig(String ... str) {
        StringBuilder sb = new StringBuilder(256);
        for(String s: str) {
            sb.append(s);
        }
        return UUID.nameUUIDFromBytes(sb.toString().getBytes());
    }
}
