package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Quota;
import com.breakersoft.plow.QuotaE;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.QuotaDao;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class QuotaDaoImpl extends AbstractDao implements QuotaDao {

    public static final RowMapper<Quota> MAPPER = new RowMapper<Quota>() {
        @Override
        public Quota mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            QuotaE quota = new QuotaE();
            quota.setQuotaId((UUID)rs.getObject(1));
            quota.setClusterId((UUID)rs.getObject(2));
            quota.setProjectId((UUID)rs.getObject(3));
            return quota;
        }
    };

    private static final String GET =
            "SELECT " +
                "pk_quota,"+
                "pk_cluster,"+
                "pk_project " +
            "FROM " +
                "plow.quota " +
            "WHERE " +
                "pk_quota=?";

    @Override
    public Quota get(UUID id) {
        return jdbc.queryForObject(GET, MAPPER, id);
    }

    private static final String INSERT =
        JdbcUtils.Insert("plow.quota",
                "pk_quota",
                "pk_project",
                "pk_cluster",
                "int_size",
                "int_burst");

    @Override
    public Quota create(Project project, Cluster cluster, int size, int burst) {

        final UUID id = UUID.randomUUID();

        jdbc.update(INSERT, id,
                project.getProjectId(),
                cluster.getClusterId(),
                size,
                burst);

        final QuotaE quota = new QuotaE();
        quota.setQuotaId(id);
        quota.setProjectId(project.getProjectId());
        quota.setClusterId(cluster.getClusterId());
        return quota;
    }

    public Quota getQuota(UUID id) {
        return null;
    }

    private static final String GET_BY_TASK =
            "SELECT " +
                "quota.pk_quota,"+
                "quota.pk_cluster,"+
                "quota.pk_project " +
            "FROM " +
                "plow.quota, " +
                "plow.job " +
            "WHERE " +
                "job.pk_project = quota.pk_project " +
            "AND " +
                "job.pk_job = ? " +
            "AND " +
                "quota.pk_cluster = ?";

    @Override
    public Quota getQuota(Node node, Task task) {
        return jdbc.queryForObject(GET_BY_TASK, MAPPER,
                task.getJobId(), node.getClusterId());
    }

    private static final String GET_BY_PROC =
            "SELECT " +
                "quota.pk_quota,"+
                "quota.pk_cluster,"+
                "quota.pk_project " +
            "FROM " +
                "plow.quota, " +
                "plow.job, " +
                "plow.node " +
            "WHERE " +
                "job.pk_project = quota.pk_project " +
            "AND " +
                "quota.pk_cluster = node.pk_cluster " +
            "AND " +
                "job.pk_job = ? " +
            "AND " +
                "node.pk_node = ?";

    @Override
    public Quota getQuota(Proc proc) {
        return jdbc.queryForObject(GET_BY_PROC, MAPPER,
                proc.getJobId(), proc.getNodeId());
    }

    private static final String ALLOCATE_RESOURCES =
            "UPDATE " +
                "plow.quota " +
            "SET " +
                "int_cores_run = int_cores_run + ? " +
            "WHERE " +
                "quota.pk_project = ? " +
            "AND " +
                "quota.pk_cluster = ? ";

    @Override
    public void allocate(Cluster cluster, Project project, int cores) {
        // Relies on check constraint to throw.
        jdbc.update(ALLOCATE_RESOURCES, cores, project.getProjectId(), cluster.getClusterId());
    }

    private static final String FREE_RESOURCE =
            "UPDATE " +
                "plow.quota " +
            "SET " +
                "int_cores_run = int_cores_run - ? " +
            "WHERE " +
                "quota.pk_quota = ?";

    @Override
    public void free(Quota quota, int cores) {
        jdbc.update(FREE_RESOURCE, cores, quota.getQuotaId());
    }

    private static final String QUOTA_CHECK =
        "SELECT " +
            "COUNT(1) " +
        "FROM " +
            "plow.quota " +
        "WHERE " +
            "quota.pk_project = ? " +
        "AND " +
            "quota.pk_cluster = ? " +
        "AND " +
            "int_cores_run + ? < int_burst ";

    @Override
    public boolean check(Cluster cluster, Project project, int cores) {
        return jdbc.queryForObject(QUOTA_CHECK, Integer.class, project.getProjectId(), cluster.getClusterId(), cores) == 1;
    }

    @Override
    public void setSize(Quota quota, int size) {
        jdbc.update("UPDATE plow.quota SET int_size=? WHERE pk_quota=?", size, quota.getQuotaId());
    }

    @Override
    public void setBurst(Quota quota, int burst) {
        jdbc.update("UPDATE plow.quota SET int_burst=? WHERE pk_quota=?", burst, quota.getQuotaId());
    }

    @Override
    public void setLocked(Quota quota, boolean locked) {
        jdbc.update("UPDATE plow.quota SET bool_locked=? WHERE pk_quota=?", locked, quota.getQuotaId());
    }


}
