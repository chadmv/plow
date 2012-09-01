package com.breakersoft.plow.util;

import org.apache.commons.lang.StringUtils;

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
		sb.append("WHERE ");
		sb.append(keyCol);
		sb.append("=?");
		return sb.toString();
	}
}
