package com.breakersoft.plow.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;

import com.breakersoft.plow.thrift.TaskTotalsT;

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


}
