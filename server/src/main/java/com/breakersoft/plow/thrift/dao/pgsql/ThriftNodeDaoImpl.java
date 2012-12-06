package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.LockState;
import com.breakersoft.plow.thrift.NodeFilterT;
import com.breakersoft.plow.thrift.NodeState;
import com.breakersoft.plow.thrift.NodeSystemT;
import com.breakersoft.plow.thrift.NodeT;
import com.breakersoft.plow.thrift.dao.ThriftNodeDao;

@Repository
@Transactional(readOnly = true)
public class ThriftNodeDaoImpl extends AbstractDao implements ThriftNodeDao {

    public static final RowMapper<NodeT> MAPPER = new RowMapper<NodeT>() {

        @Override
        public NodeT mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            NodeT node = new NodeT();
            node.setId(rs.getString("pk_node"));
            node.setName(rs.getString("str_name"));
            node.setLockState(LockState.findByValue(
                    rs.getInt("int_lock_state")));
            node.setState(NodeState.findByValue(
                    rs.getInt("int_state")));

            node.setClusterId(rs.getString("pk_cluster"));
            node.setClusterName(rs.getString("cluster_name"));

            node.setBootTime(rs.getLong("time_booted"));
            node.setUpdatedTime(rs.getLong("time_updated"));
            node.setCreatedTime(rs.getLong("time_created"));

            node.setTotalRamMb(rs.getInt("int_ram"));
            node.setFreeRamMb(rs.getInt("int_free_ram"));
            node.setTotalCores(rs.getInt("int_cores"));
            node.setIdleCores(rs.getInt("int_idle_cores"));

            NodeSystemT system = new NodeSystemT();
            system.setCpuModel(rs.getString("str_cpu_model"));
            system.setPlatform(rs.getString("str_platform"));

            system.setTotalSwapMb(rs.getInt("int_swap"));
            system.setFreeSwapMb(rs.getInt("int_free_swap"));

            system.setTotalRamMb(rs.getInt("int_ram_sys"));
            system.setFreeRamMb(rs.getInt("int_free_ram_sys"));

            system.setLoad(new ArrayList<Integer>());
            system.setLogicalCores(rs.getInt("int_log_cores"));
            system.setPhysicalCores(rs.getInt("int_phys_cores"));

            node.setSystem(system);

            return node;

        }
    };

    private static final String GET =
            "SELECT " +
                "node.pk_node,"+
                "node.pk_cluster,"+
                "node.str_name,"+
                "node.int_state, "+
                "node.int_lock_state,"+
                "node.time_created,"+
                "node.time_updated,"+
                "node.str_tags,"+

                "node_sys.int_phys_cores,"+
                "node_sys.int_log_cores,"+
                "node_sys.int_ram AS int_ram_sys, "+
                "node_sys.int_free_ram AS int_free_ram_sys,"+
                "node_sys.int_swap,"+
                "node_sys.int_free_swap,"+
                "node_sys.time_booted,"+
                "node_sys.str_cpu_model,"+
                "node_sys.str_platform, " +

                "node_dsp.int_cores, "+
                "node_dsp.int_idle_cores, "+
                "node_dsp.int_ram,"+
                "node_dsp.int_free_ram, " +

                "cluster.str_name AS cluster_name " +
            "FROM " +
                "node " +
                    "INNER JOIN node_sys ON node.pk_node = node_sys.pk_node " +
                    "INNER JOIN node_dsp ON node.pk_node = node_dsp.pk_node " +
                    "INNER JOIN cluster ON node.pk_cluster = cluster.pk_cluster ";

    @Override
    public List<NodeT> getNodes(NodeFilterT filter) {
        return jdbc.query(GET, MAPPER);
    }

    private final String GET_BY_ID = GET + " WHERE node.pk_node=?";

    @Override
    public NodeT getNode(UUID id) {
        return jdbc.queryForObject(GET_BY_ID, MAPPER, id);
    }
}
