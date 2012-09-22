package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.NodeE;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.NodeDao;
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
                "int_boot_time",
                "int_created_time",
                "int_ping_time"),

        JdbcUtils.Insert("plow.node_status",
                "pk_node",
                "int_phys_cores",
                "int_log_cores",
                "int_memory",
                "int_free_memory",
                "int_swap",
                "int_free_swap",
                "str_proc",
                "str_os"),

        JdbcUtils.Insert("plow.node_dsp",
                "pk_node",
                "int_cores",
                "int_free_cores",
                "int_memory",
                "int_free_memory")
    };

    @Override
    public Node create(Cluster cluster, Ping ping) {

        final UUID id = UUID.randomUUID();
        final long time = System.currentTimeMillis();

        jdbc.update(INSERT[0], id, cluster.getClusterId(),
                ping.hostname,
                ping.ipAddr,
                ping.bootTime,
                time,
                time);

        jdbc.update(INSERT[1], id,
                ping.hw.physicalCpus,
                ping.hw.logicalCpus,
                ping.hw.totalRamMb,
                ping.hw.freeRamMb,
                ping.hw.totalSwapMb,
                ping.hw.freeSwapMb,
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

    private int getBookableMemory(int memory) {
        return memory = memory - Defaults.MEMORY_RESERVE_MB;
    }

}
