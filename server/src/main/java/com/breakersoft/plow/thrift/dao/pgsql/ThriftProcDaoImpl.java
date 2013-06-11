package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.ProcFilterT;
import com.breakersoft.plow.thrift.ProcT;
import com.breakersoft.plow.thrift.dao.ThriftProcDao;
import com.breakersoft.plow.util.JdbcUtils;
import com.breakersoft.plow.util.PlowUtils;
import com.google.common.collect.Lists;

@Repository
@Transactional(readOnly=true)
public class ThriftProcDaoImpl extends AbstractDao implements ThriftProcDao {

    private static final RowMapper<ProcT> MAPPER = new RowMapper<ProcT>() {

        @Override
        public ProcT mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            final ProcT proc = new ProcT();
            proc.setId(rs.getString("pk_proc"));
            proc.setNodeId(rs.getString("pk_node"));
            proc.setJobName(rs.getString("job_name"));
            proc.setLayerName(rs.getString("layer_name"));
            proc.setTaskName(rs.getString("task_name"));
            proc.setCores(rs.getInt("int_cores"));
            proc.setUsedCores(rs.getDouble("flt_cores_used"));
            proc.setHighCores(rs.getDouble("flt_cores_high"));
            proc.setRam(rs.getInt("int_ram"));
            proc.setHighRam(rs.getInt("int_ram_high"));
            proc.setUsedRam(rs.getInt("int_ram_used"));
            // TODO: fix io stats
            proc.setIoStats(new ArrayList<Long>(4));
            proc.setCreatedTime(rs.getLong("time_created"));
            proc.setUpdatedTime(rs.getLong("time_updated"));
            proc.setStartedTime(rs.getLong("time_started"));
            return proc;
        }
    };

    private static final String GET =
        "SELECT " +
            "proc.pk_proc,"+
            "proc.pk_node,"+
            "proc.pk_task,"+
            "proc.int_cores, " +
            "proc.flt_cores_used," +
            "proc.flt_cores_high,"+
            "proc.int_ram,"+
            "proc.int_ram_used,"+
            "proc.int_ram_high, "+
            "proc.time_created,"+
            "proc.time_updated,"+
            "proc.time_started,"+
            "proc.int_progress,"+
            "job.str_name AS job_name,"+
            "layer.str_name AS layer_name,"+
            "task.str_name AS task_name "+
        "FROM " +
            "plow.proc " +
                "INNER JOIN plow.job ON proc.pk_job = job.pk_job " +
                "INNER JOIN plow.layer ON proc.pk_layer = layer.pk_layer " +
                "INNER JOIN plow.task ON proc.pk_task = task.pk_task ";
    @Override
    public List<ProcT> getProcs(ProcFilterT filter) {

        final List<String> clauses = Lists.newArrayListWithExpectedSize(8);
        final List<Object> values = Lists.newArrayList();

        if (PlowUtils.isValid(filter.projectIds)) {
            clauses.add(JdbcUtils.In(
                    "job.pk_project", filter.projectIds.size(), "uuid"));
            values.addAll(filter.projectIds);
        }

        if (PlowUtils.isValid(filter.folderIds)) {
            clauses.add(JdbcUtils.In(
                    "job.pk_folder", filter.folderIds.size(), "uuid"));
            values.addAll(filter.folderIds);
        }

        if (PlowUtils.isValid(filter.jobIds)) {
            clauses.add(JdbcUtils.In(
                    "job.pk_job", filter.jobIds.size(), "uuid"));
            values.addAll(filter.jobIds);
        }

        if (PlowUtils.isValid(filter.layerIds)) {
            clauses.add(JdbcUtils.In(
                    "proc.pk_layer", filter.layerIds.size(), "uuid"));
            values.addAll(filter.layerIds);
        }

        if (PlowUtils.isValid(filter.taskIds)) {
            clauses.add(JdbcUtils.In(
                    "proc.pk_task", filter.taskIds.size(), "uuid"));
            values.addAll(filter.taskIds);
        }

        if (PlowUtils.isValid(filter.clusterIds)) {
            clauses.add(JdbcUtils.In(
                    "proc.pk_cluster", filter.clusterIds.size(), "uuid"));
            values.addAll(filter.clusterIds);
        }

        if (PlowUtils.isValid(filter.quotaIds)) {
            clauses.add(JdbcUtils.In(
                    "proc.pk_quota", filter.quotaIds.size(), "uuid"));
            values.addAll(filter.quotaIds);
        }

        if (PlowUtils.isValid(filter.nodeIds)) {
            clauses.add(JdbcUtils.In(
                    "proc.pk_node", filter.nodeIds.size(), "uuid"));
            values.addAll(filter.nodeIds);
        }

        if (filter.getLastUpdateTime() > 0) {
            clauses.add("proc.time_updated >= ?");
            values.add(filter.getLastUpdateTime());
        }

        final StringBuilder sb = new StringBuilder(512);
        sb.append(GET);
        if (!values.isEmpty()) {
            sb.append(" WHERE " );
            sb.append(StringUtils.join(clauses, " AND "));
        }

        if (filter.limit > 0) {
            sb.append(" LIMIT " + filter.limit);
        }

        if (filter.offset > 0) {
            sb.append(" OFFSET " + filter.offset);
        }

        return jdbc.query(sb.toString(), MAPPER, values.toArray());
    }

    @Override
    public ProcT getProc(UUID id) {
        return jdbc.queryForObject(GET + " WHERE proc.pk_proc=?", MAPPER, id);
    }
}
