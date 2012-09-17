package com.breakersoft.plow.dao.pgsql;

import java.util.UUID;

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
                "int_cores",
                "int_memory",
                "int_free_memory",
                "int_swap",
                "int_free_swap",
                "int_ht_factor",
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
                ping.hw.totalCores,
                ping.hw.totalMemory,
                ping.hw.freeMemory,
                ping.hw.totalSwap,
                ping.hw.freeSwap,
                ping.hw.htFactor,
                ping.hw.procModel,
                ping.hw.osName);

        final int memMb =
                getBookableMemory(ping.hw.totalMemory);
        jdbc.update(INSERT[2], id,
                ping.hw.totalCores,
                ping.hw.totalCores,
                memMb,
                memMb);

        NodeE node = new NodeE();
        node.setNodeId(id);
        node.setName(ping.hostname);
        return node;
    }

    private int getBookableMemory(int memory) {
        return memory = memory - Defaults.MEMORY_RESERVE_MB;
    }

}
