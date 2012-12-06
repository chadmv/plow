package com.breakersoft.plow.dao.pgsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.NodeE;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.NodeDao;
import com.breakersoft.plow.exceptions.ResourceAllocationException;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class NodeDaoImpl extends AbstractDao implements NodeDao {

    private static final String INSERT[] = {
        JdbcUtils.Insert("plow.node",
                "pk_node",
                "pk_cluster",
                "str_name",
                "str_ipaddr",
                "str_tags"),

        JdbcUtils.Insert("plow.node_sys",
                "pk_node",
                "int_phys_cores",
                "int_log_cores",
                "int_ram",
                "int_free_ram",
                "int_swap",
                "int_free_swap",
                "time_booted",
                "str_cpu_model",
                "str_platform"),

        JdbcUtils.Insert("plow.node_dsp",
                "pk_node",
                "int_cores",
                "int_idle_cores",
                "int_ram",
                "int_free_ram")
    };

    @Override
    public Node create(final Cluster cluster, final Ping ping) {

        final UUID id = UUID.randomUUID();

        final String clusterTag = jdbc.queryForObject(
                "SELECT str_tag FROM plow.cluster WHERE pk_cluster=?",
                String.class, cluster.getClusterId());

        jdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(INSERT[0]);
                ps.setObject(1, id);
                ps.setObject(2, cluster.getClusterId());
                ps.setString(3, ping.hostname);
                ps.setString(4, ping.ipAddr);
                ps.setArray(5, conn.createArrayOf("text", new String[] { clusterTag }));
                return ps;
            }
        });

        jdbc.update(INSERT[1], id,
                ping.hw.physicalCpus,
                ping.hw.logicalCpus,
                ping.hw.totalRamMb,
                ping.hw.freeRamMb,
                ping.hw.totalSwapMb,
                ping.hw.freeSwapMb,
                ping.bootTime,
                ping.hw.cpuModel,
                ping.hw.platform);

        final int memMb =
                getBookableMemory(ping.hw.totalRamMb);
        jdbc.update(INSERT[2], id,
                ping.hw.physicalCpus,
                ping.hw.physicalCpus,
                memMb,
                memMb);

        NodeE node = new NodeE();
        node.setNodeId(id);
        node.setClusterId(cluster.getClusterId());
        node.setName(ping.hostname);
        return node;
    }

    public static final RowMapper<Node> MAPPER = new RowMapper<Node>() {
        @Override
        public Node mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            NodeE node = new NodeE();
            node.setNodeId((UUID) rs.getObject(1));
            node.setClusterId((UUID) rs.getObject(2));
            node.setName(rs.getString(3));
            return node;
        }
    };

    private static final String GET_BY_NAME =
            "SELECT " +
                "pk_node, " +
                "pk_cluster, "+
                "str_name " +
            "FROM " +
                "plow.node " +
            "WHERE " +
                "str_name = ?";

    @Override
    public Node get(String hostname) {
        return jdbc.queryForObject(GET_BY_NAME, MAPPER, hostname);
    }

    private static final String GET_BY_ID =
            "SELECT " +
                "pk_node, " +
                "pk_cluster, "+
                "str_name " +
            "FROM " +
                "plow.node " +
            "WHERE " +
                "node.pk_node=?";

    @Override
    public Node get(UUID id) {
        return jdbc.queryForObject(GET_BY_ID, MAPPER, id);
    }

    private int getBookableMemory(int memory) {
        return memory = memory - Defaults.MEMORY_RESERVE_MB;
    }

    private static final String ALLOCATE_RESOURCES =
            "UPDATE " +
                "plow.node_dsp " +
            "SET " +
                "int_idle_cores = int_idle_cores - ?," +
                "int_free_ram = int_free_ram - ? " +
            "WHERE " +
                "node_dsp.int_idle_cores >= ? " +
            "AND " +
                "node_dsp.int_free_ram >= ? " +
            "AND " +
                "node_dsp.pk_node = ? ";

    @Override
    public void allocateResources(Node node, int cores, int memory) {
        if (jdbc.update(ALLOCATE_RESOURCES,
                cores, memory, cores, memory, node.getNodeId()) != 1) {
            String msg = String.format("Failed to allocate %d/%d from %s",
                    cores, memory, node.getName());
            throw new ResourceAllocationException(msg);
        }
    }

    private static final String FREE_RESOURCES =
            "UPDATE " +
                "plow.node_dsp " +
            "SET " +
                "int_idle_cores = int_idle_cores + ?," +
                "int_free_ram = int_free_ram + ? " +
            "WHERE " +
                "node_dsp.pk_node = ? ";

    @Override
    public void freeResources(Node node, int cores, int memory) {
        jdbc.update(FREE_RESOURCES, cores, memory, node.getNodeId());
    }
}
