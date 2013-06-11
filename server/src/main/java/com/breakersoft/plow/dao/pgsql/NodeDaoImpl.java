package com.breakersoft.plow.dao.pgsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
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
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.thrift.NodeState;
import com.breakersoft.plow.thrift.SlotMode;
import com.breakersoft.plow.util.JdbcUtils;
import com.breakersoft.plow.util.PlowUtils;
import com.google.common.base.Preconditions;

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
                "str_platform",
                "flt_load"),

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
        final List<String> tags = jdbc.queryForList("SELECT unnest(str_tags) " +
                "FROM plow.cluster WHERE pk_cluster=?", String.class, cluster.getClusterId());

        jdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(INSERT[0]);
                ps.setObject(1, id);
                ps.setObject(2, cluster.getClusterId());
                ps.setString(3, ping.hostname);
                ps.setString(4, ping.ipAddr);
                ps.setArray(5, conn.createArrayOf("text", tags.toArray()));
                return ps;
            }
        });

        jdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(INSERT[1]);
                ps.setObject(1, id);
                ps.setInt(2, ping.hw.physicalCpus);
                ps.setInt(3, ping.hw.logicalCpus);
                ps.setInt(4, ping.hw.totalRamMb);
                ps.setInt(5, ping.hw.freeRamMb);
                ps.setInt(6, ping.hw.totalSwapMb);
                ps.setInt(7, ping.hw.freeSwapMb);
                ps.setLong(8, ping.bootTime * 1000);
                ps.setString(9, ping.hw.cpuModel);
                ps.setString(10, ping.hw.platform);

                if (!PlowUtils.isValid(ping.hw.load)) {
                    ps.setArray(11, conn.createArrayOf("float", new Float[] { 0.0f, 0.0f, 0.0f }));
                }
                else {
                    ps.setArray(11, conn.createArrayOf("float",  ping.hw.load.toArray()));
                }
                return ps;
            }
        });

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

    private static final String FULL_UPDATE =
        JdbcUtils.Update("plow.node_sys",
            "pk_node",
            "int_phys_cores",
            "int_log_cores",
            "int_ram",
            "int_free_ram",
            "int_swap",
            "int_free_swap",
            "time_booted",
            "str_cpu_model",
            "str_platform");

    @Override
    public void update(Node node, Ping ping) {

        jdbc.update("UPDATE plow.node SET " +
                "time_updated=plow.txTimeMillis() WHERE pk_node=?", node.getNodeId());

        jdbc.update("UPDATE plow.node SET " +
                "int_state=? WHERE pk_node=? AND int_state=?",
                NodeState.UP.ordinal(), node.getNodeId(), NodeState.DOWN.ordinal());

        jdbc.update(FULL_UPDATE,
                ping.hw.physicalCpus,
                ping.hw.logicalCpus,
                ping.hw.totalRamMb,
                ping.hw.freeRamMb,
                ping.hw.totalSwapMb,
                ping.hw.freeSwapMb,
                ping.bootTime * 1000,
                ping.hw.cpuModel,
                ping.hw.platform,
                node.getNodeId());
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

    private static final String GET_UNRESPONSIVE =
            "SELECT " +
                "pk_node, " +
                "pk_cluster, "+
                "str_name " +
            "FROM " +
                "plow.node " +
            "WHERE " +
                "node.int_state = ? " +
            "AND " +
                "plow.currentTimeMillis() - node.time_updated >= ? " +
            "LIMIT 50";

    @Override
    public List<Node> getUnresponsiveNodes() {
        return jdbc.query(GET_UNRESPONSIVE, MAPPER, NodeState.UP.ordinal(), Defaults.NODE_UNRESPONSIVE_MS);
    }

    private int getBookableMemory(int memory) {
        return memory = memory - Defaults.NODE_RESERVE_MEMORY;
    }

    private static final String ALLOCATE_RESOURCES =
            "UPDATE " +
                "plow.node_dsp " +
            "SET " +
                "int_idle_cores = int_idle_cores - ?," +
                "int_free_ram = int_free_ram - ? " +
            "WHERE " +
                "node_dsp.pk_node = ? ";

    @Override
    public void allocate(Node node, int cores, int memory) {
        // Check constraints will throw.
        jdbc.update(ALLOCATE_RESOURCES,
                cores, memory, node.getNodeId());
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
    public void free(Node node, int cores, int memory) {
        jdbc.update(FREE_RESOURCES, cores, memory, node.getNodeId());
    }

    @Override
    public void setLocked(Node node, boolean locked) {
        jdbc.update("UPDATE plow.node SET bool_locked=? WHERE pk_node=?", locked, node.getNodeId());
    }

    @Override
    public boolean hasProcs(Node node, boolean lock) {
        String query = "SELECT int_cores - int_idle_cores FROM plow.node_dsp WHERE pk_node=?";
        if (lock) {
            query = query + " FOR UPDATE";
        }
        return jdbc.queryForObject(query, Integer.class, node.getNodeId()) != 0;
    }

    @Override
    public void setCluster(Node node, Cluster cluster) {
        jdbc.update("UPDATE plow.node SET pk_cluster=? WHERE pk_node=?",
                cluster.getClusterId(), node.getNodeId());
    }

    private static final String UPDATE_TAGS =
            "UPDATE plow.node SET str_tags=? WHERE pk_node=?";

    @Override
    public void setTags(final Node node, final Set<String> tags) {
         jdbc.update(new PreparedStatementCreator() {
             @Override
             public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                 final PreparedStatement ret = conn.prepareStatement(UPDATE_TAGS);
                 ret.setObject(1, JdbcUtils.toArray(conn, tags));
                 ret.setObject(2, node.getNodeId());
                 return ret;
             }
         });
    }

    @Override
    public boolean setState(Node node, NodeState state) {
        return jdbc.update("UPDATE plow.node SET int_state=? WHERE pk_node=? AND int_state!=?",
                state.ordinal(), node.getNodeId(), state.ordinal()) == 1;
    }

    private static final String UPDATE_SLOT_MODE =
            "UPDATE " +
                "plow.node " +
            "SET " +
                "int_slot_mode=?,"+
                "int_slot_cores=?,"+
                "int_slot_ram=? "+
            "WHERE " +
                "pk_node=?";

    @Override
    public void setSlotMode(Node node, SlotMode mode, int cores, int ram) {
        if (mode.equals(SlotMode.SINGLE) || mode.equals(SlotMode.DYNAMIC)) {
            cores = 0;
            ram = 0;
        }
        else if (mode.equals(SlotMode.SLOTS)) {
            Preconditions.checkArgument(cores > 0, "Cores must be greater than 0 in slot mode");
            Preconditions.checkArgument(ram > 0, "Ram must be greater than 0 in slot mode");
        }

        jdbc.update(UPDATE_SLOT_MODE, mode.ordinal(), cores, ram, node.getNodeId());
    }
}
