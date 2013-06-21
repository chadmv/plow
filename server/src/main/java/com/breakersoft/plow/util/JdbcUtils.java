package com.breakersoft.plow.util;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.breakersoft.plow.thrift.TaskTotalsT;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public final class JdbcUtils {

    public static String Insert(String table, String ... cols) {
        final StringBuilder sb = new StringBuilder(1024);
        sb.append("INSERT INTO ");
        sb.append(table);
        sb.append("(");
        sb.append(StringUtils.join(cols, ","));
        sb.append(") VALUES (");
        sb.append(StringUtils.repeat("?",",", cols.length));
        sb.append(")");
        return sb.toString();
    }

    public static String Update(String table, String keyCol, String ... cols) {
        final StringBuilder sb = new StringBuilder(1024);
        sb.append("UPDATE ");
        sb.append(table);
        sb.append(" SET ");
        for (String col: cols) {
            sb.append(col);
            sb.append("=?,");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(" WHERE ");
        sb.append(keyCol);
        sb.append("=?");
        return sb.toString();
    }

    public static String In(String col, int size) {
        return String.format("%s IN (%s)", col,
                StringUtils.repeat("?",",", size));
    }

    public static String In(String col, int size, String cast) {
        final String repeat = "?::" + cast;
        return String.format("%s IN (%s)", col,
                StringUtils.repeat(repeat,",", size));
    }

    public static final String limitOffset(int limit, int offset) {
        return String.format("LIMIT %d OFFSET %d", limit, offset);
    }

    public static TaskTotalsT getTaskTotals(ResultSet rs) throws SQLException {
        TaskTotalsT t = new TaskTotalsT();
        t.setTotalTaskCount(rs.getInt("int_total"));
        t.setSucceededTaskCount(rs.getInt("int_succeeded"));
        t.setRunningTaskCount(rs.getInt("int_running"));
        t.setDeadTaskCount(rs.getInt("int_dead"));
        t.setEatenTaskCount(rs.getInt("int_eaten"));
        t.setWaitingTaskCount(rs.getInt("int_waiting"));
        t.setDependTaskCount(rs.getInt("int_depend"));
        return t;
    }

    public static final Array toArray(Connection conn, Collection<String> col) throws SQLException {
        return conn.createArrayOf("text", col.toArray());
    }

    public static final String toIntRange(int[] range) throws SQLException {
        return String.format("[%d,%d]", range[0], range[1]);
    }

    public static final ImmutableList<String> toList(Array sqlArray) {
        if (sqlArray == null) {
            return ImmutableList.of();
        }

        try {
            return ImmutableList.copyOf((String[])sqlArray.getArray());
        } catch (SQLException e) {
            return ImmutableList.of();
        }
    }

    public static final ImmutableSet<String> toSet(Array sqlArray) {
        if (sqlArray == null) {
            return ImmutableSet.of();
        }

        try {
            return ImmutableSet.copyOf((String[])sqlArray.getArray());
        } catch (SQLException e) {
            return ImmutableSet.of();
        }
    }


    public static final RowMapper<String> STRING_MAPPER = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            return rs.getString(1);
        }
    };

    public static final RowMapper<Object[]> OBJECT_ARRAY_MAPPER = new RowMapper<Object[]>() {
        @Override
        public Object[] mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            final int count = rs.getMetaData().getColumnCount();
            Object[] result = new Object[rs.getMetaData().getColumnCount()];
            for (int i=0; i<count; i++) {
                result[i] = rs.getObject(i+1);
            }
            return result;
        }
    };
}

