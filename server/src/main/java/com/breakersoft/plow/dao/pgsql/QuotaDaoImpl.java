package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Node;
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
}
