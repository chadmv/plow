package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.ClusterCountsT;
import com.breakersoft.plow.thrift.ClusterT;
import com.breakersoft.plow.thrift.dao.ThriftClusterDao;
import com.breakersoft.plow.util.PlowUtils;

@Repository
public class ThriftClusterDaoImpl extends AbstractDao implements ThriftClusterDao {

    public static final RowMapper<ClusterT> MAPPER = new RowMapper<ClusterT>() {

        @Override
        public ClusterT mapRow(ResultSet rs, int rowNum)
                throws SQLException {
        	ClusterT cluster = new ClusterT();
        	cluster.id = rs.getString("pk_cluster");
        	cluster.name = rs.getString("str_name");
        	cluster.isDefault = rs.getBoolean("bool_default");
        	cluster.isLocked = rs.getBoolean("bool_locked");
        	cluster.tag = rs.getString("str_tag");

        	cluster.total = new ClusterCountsT();
        	cluster.total.setNodes(rs.getInt("node_total"));
        	cluster.total.setLockedNodes(rs.getInt("node_locked_total"));
        	cluster.total.setUnlockedNodes(cluster.total.nodes - cluster.total.lockedNodes);
        	cluster.total.setUpNodes(rs.getInt("node_up_total"));
        	cluster.total.setDownNodes(rs.getInt("node_down_total"));
        	cluster.total.setRepairNodes(rs.getInt("node_repair_total"));
        	cluster.total.setCores(rs.getInt("core_total"));
        	cluster.total.setIdleCores(rs.getInt("core_idle_total"));
        	cluster.total.setRunCores(cluster.total.cores - cluster.total.idleCores);
        	cluster.total.setUpCores(rs.getInt("core_up_total"));
        	cluster.total.setDownCores(rs.getInt("core_down_total"));
        	cluster.total.setRepairCores(rs.getInt("core_repair_total"));
        	cluster.total.setLockedCores(rs.getInt("core_locked_total"));
        	cluster.total.setUnlockedCores(cluster.total.cores - cluster.total.lockedCores);

        	return cluster;
        }
    };

    private static final String GET_CLUSTER  =
    	"SELECT " +
    		"cluster.pk_cluster,"+
    		"cluster.str_name,"+
    		"cluster.str_tag,"+
    		"cluster.bool_locked,"+
    		"cluster.bool_default," +
    		"COALESCE(cluster_totals.node_total,0) AS node_total," +
    		"COALESCE(cluster_totals.node_locked_total,0) AS node_locked_total, " +
    		"COALESCE(cluster_totals.node_up_total,0) AS node_up_total, " +
    		"COALESCE(cluster_totals.node_down_total,0) AS node_down_total, " +
    		"COALESCE(cluster_totals.node_repair_total,0) AS node_repair_total, " +
    		"COALESCE(cluster_totals.core_total,0) AS core_total, " +
    		"COALESCE(cluster_totals.core_idle_total,0) AS core_idle_total, " +
    		"COALESCE(cluster_totals.core_locked_total,0) AS core_locked_total, " +
    		"COALESCE(cluster_totals.core_up_total,0) AS core_up_total, " +
    		"COALESCE(cluster_totals.core_down_total,0) AS core_down_total, " +
    		"COALESCE(cluster_totals.core_repair_total,0) AS core_repair_total, " +
    		"COALESCE(cluster_totals.core_locked_total,0) AS core_locked_total " +
    	"FROM " +
    		"plow.cluster " +
    	"LEFT JOIN " +
    		"plow.cluster_totals ON cluster.pk_cluster = cluster_totals.pk_cluster ";

    private static final String GET_CLUSTER_BY_ID = GET_CLUSTER + "WHERE cluster.pk_cluster = ?::uuid";
    private static final String GET_CLUSTER_BY_NAME = GET_CLUSTER + "WHERE cluster.str_name = ?";

    @Override
    public ClusterT getCluster(String id) {

		if (PlowUtils.isUuid(id)) {
			return jdbc.queryForObject(GET_CLUSTER_BY_ID, MAPPER, id);
		}
		else {
			return  jdbc.queryForObject(GET_CLUSTER_BY_NAME, MAPPER, id);
		}
	}

	@Override
	public List<ClusterT> getClusters() {
		return jdbc.query(GET_CLUSTER, MAPPER);
	}

    private static final String GET_CLUSTERS_BY_TAG = GET_CLUSTER + "WHERE cluster.str_tag = ?";

    @Override
	public List<ClusterT> getClusters(String tag) {
		return jdbc.query(GET_CLUSTERS_BY_TAG, MAPPER, tag);
	}
}
