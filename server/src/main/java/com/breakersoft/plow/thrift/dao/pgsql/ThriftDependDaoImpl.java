package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.DependT;
import com.breakersoft.plow.thrift.DependType;
import com.breakersoft.plow.thrift.dao.ThriftDependDao;

@Repository
@Transactional(readOnly=true)
public class ThriftDependDaoImpl extends AbstractDao implements ThriftDependDao {

    public static final RowMapper<DependT> MAPPER = new RowMapper<DependT>() {
        @Override
        public DependT mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            final DependT depend = new DependT();
            depend.setId(rs.getString("pk_depend"));
            depend.setActive(rs.getBoolean("bool_active"));
            depend.setType(DependType.findByValue(rs.getInt("int_type")));
            depend.setDependentJobId(rs.getString("pk_dependent_job"));
            depend.setDependOnJobId(rs.getString("pk_dependon_job"));
            depend.setDependentJobName(rs.getString("str_dependent_job_name"));
            depend.setDependOnJobName(rs.getString("str_dependon_job_name"));
            depend.setCreatedTime(rs.getLong("time_created"));
            depend.setSatisfiedTime(rs.getLong("time_satisfied"));

            if (depend.getType().equals(DependType.JOB_ON_JOB)) {
                return depend;
            }

            depend.setDependentLayerId(rs.getString("pk_dependent_layer"));
            depend.setDependOnLayerId(rs.getString("pk_dependon_layer"));
            depend.setDependentLayerName(rs.getString("str_dependent_layer_name"));
            depend.setDependOnLayerName(rs.getString("str_dependon_layer_name"));

            switch (depend.getType()) {

                case LAYER_ON_TASK:
                    depend.setDependOnTaskId(rs.getString("pk_dependon_task"));
                    depend.setDependOnTaskName(rs.getString("str_dependon_task_name"));
                    break;
                case TASK_ON_LAYER:
                    depend.setDependentTaskId(rs.getString("pk_dependent_task"));
                    depend.setDependentTaskName(rs.getString("str_dependent_task_name"));
                    break;
                case TASK_ON_TASK:
                    depend.setDependentTaskId(rs.getString("pk_dependent_task"));
                    depend.setDependOnTaskId(rs.getString("pk_dependon_task"));
                    depend.setDependentTaskName(rs.getString("str_dependent_task_name"));
                    depend.setDependOnTaskName(rs.getString("str_dependon_task_name"));
                    break;
            }

            return depend;

        }
    };

    private static final String GET =
            "SELECT " +
                "pk_depend,"+
                "int_type,"+
                "bool_active,"+
                "pk_dependent_job," +
                "pk_dependon_job," +
                "pk_dependent_layer," +
                "pk_dependon_layer," +
                "pk_dependent_task," +
                "pk_dependon_task," +
                "str_dependent_job_name," +
                "str_dependon_job_name," +
                "str_dependent_layer_name," +
                "str_dependon_layer_name," +
                "str_dependent_task_name," +
                "str_dependon_task_name," +
                "time_created," +
                "time_satisfied " +
            "FROM " +
                "plow.depend ";

    @Override
    public DependT getDepend(UUID dependId) {
        return jdbc.queryForObject(GET + " WHERE pk_depend=?", MAPPER, dependId);
    }

    @Override
    public List<DependT> getWhatDependsOnJob(UUID jobId) {
        return jdbc.query(GET + " WHERE pk_dependon_job=? AND int_type = ?",
                MAPPER, jobId, DependType.JOB_ON_JOB.ordinal());
    }

    @Override
    public List<DependT> getWhatDependsOnLayer(UUID layerId) {
        return jdbc.query(GET + " WHERE pk_dependon_layer=? AND int_type IN (?,?)",
                MAPPER, layerId, DependType.LAYER_ON_LAYER.ordinal(), DependType.TASK_ON_LAYER.ordinal());
    }

    @Override
    public List<DependT> getWhatDependsOnTask(UUID taskId) {
        return jdbc.query(GET + " WHERE pk_dependon_task=? AND int_type IN (?,?)",
                MAPPER, taskId, DependType.LAYER_ON_TASK.ordinal(), DependType.TASK_ON_TASK.ordinal());
    }

    @Override
    public List<DependT> getWhatJobDependsOn(UUID jobId) {
        return jdbc.query(GET + " WHERE pk_dependent_job=? AND int_type = ?",
                MAPPER, jobId, DependType.JOB_ON_JOB.ordinal());
    }

    @Override
    public List<DependT> getWhatLayerDependsOn(UUID layerId) {
        return jdbc.query(GET + " WHERE pk_dependent_layer=? AND int_type IN (?,?)",
                MAPPER, layerId, DependType.LAYER_ON_LAYER.ordinal(), DependType.LAYER_ON_TASK.ordinal());
    }

    @Override
    public List<DependT> getWhatTaskDependsOn(UUID taskId) {
        return jdbc.query(GET + " WHERE pk_dependent_task=? AND int_type IN (?,?)",
                MAPPER, taskId, DependType.TASK_ON_LAYER.ordinal(), DependType.TASK_ON_TASK.ordinal());
    }
}
