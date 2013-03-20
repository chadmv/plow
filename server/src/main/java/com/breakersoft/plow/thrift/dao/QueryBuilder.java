package com.breakersoft.plow.thrift.dao;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.breakersoft.plow.util.JdbcUtils;
import com.google.common.collect.Lists;

public final class QueryBuilder {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(QueryBuilder.class);

    final List<String> wheres = Lists.newArrayList();
    final List<Object> values = Lists.newArrayList();

    public QueryBuilder() { }

    public QueryBuilder is(String col, Object value) {
    	wheres.add(String.format("%s=?", col));
    	values.add(value);
    	return this;
    }

    public QueryBuilder in(String col, Collection<?>c) {

    	if (c.isEmpty()) {
    		return this;
    	}

    	wheres.add(JdbcUtils.In(col, c.size()));
        values.addAll(c);

    	return this;
    }

    public QueryBuilder in(String col, Collection<?>c, String cast) {

    	if (c.isEmpty()) {
    		return this;
    	}

    	wheres.add(JdbcUtils.In(col, c.size(), cast));
        values.addAll(c);

    	return this;
    }


	public String build() {
		final String q = StringUtils.join(wheres, " AND ");
		logger.trace(q);
		return q;
	}

	public String build(String query) {
		String where = build();
		if (where.isEmpty()) {
			logger.debug(query);
			return query;
		}
		else {
			final String q =  String.format("%s WHERE %s", query, where);
			logger.info(q);
			return q;
		}
	}

	public Object[] values() {
		return values.toArray();
	}
}
