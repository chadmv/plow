package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.QuotaFilterT;
import com.breakersoft.plow.thrift.QuotaT;
import com.breakersoft.plow.thrift.dao.QueryBuilder;
import com.breakersoft.plow.thrift.dao.ThriftQuotaDao;

@Repository
@Transactional(readOnly=true)
public class ThriftQuotaDaoImpl  extends AbstractDao implements ThriftQuotaDao {

	 public static final RowMapper<QuotaT> MAPPER = new RowMapper<QuotaT>() {
		@Override
		public QuotaT mapRow(ResultSet rs, int rowNum) throws SQLException {
			final QuotaT quota = new QuotaT();
			quota.setName(rs.getString("str_name"));
			quota.setClusterId(rs.getString("pk_cluster"));
			quota.setProjectId(rs.getString("pk_project"));
			quota.setId(rs.getString("pk_quota"));
			quota.setBurst(rs.getInt("int_burst"));
			quota.setSize(rs.getInt("int_burst"));
			quota.setRunCores(rs.getInt("int_run_cores"));
			return quota;
		}
	 };

	private static final String GET =
		"SELECT " +
			"quota.pk_quota,"+
			"quota.pk_project,"+
			"quota.pk_cluster,"+
			"project.str_code || '.' || cluster.str_name AS str_name, "+
			"quota.int_burst,"+
			"quota.int_size,"+
			"quota.int_run_cores "+
		"FROM " +
			"quota " +
				"INNER JOIN project ON quota.pk_project = project.pk_project " +
				"INNER JOIN cluster ON quota.pk_cluster = cluster.pk_cluster ";

	@Override
	public List<QuotaT> getQuotas(QuotaFilterT filter)  {

		QueryBuilder qb = new QueryBuilder();

        if (filter.isSetCluster()) {
        	qb.in("quota.pk_cluster", filter.cluster, "uuid");
        }

        if (filter.isSetProject()) {
        	qb.in("quota.pk_project", filter.project, "uuid");
        }

        return jdbc.query(qb.build(GET), MAPPER, qb.values());
	}

	@Override
	public QuotaT getQuota(UUID id)  {

		QueryBuilder qb = new QueryBuilder();
		qb.is("quota.pk_quota", id);

        return jdbc.queryForObject(qb.build(GET), MAPPER, id);
	}
}
