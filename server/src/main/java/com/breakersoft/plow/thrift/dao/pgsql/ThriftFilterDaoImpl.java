package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.FilterT;
import com.breakersoft.plow.thrift.dao.ThriftFilterDao;

@Repository
@Transactional(readOnly=true)
public class ThriftFilterDaoImpl extends AbstractDao implements ThriftFilterDao {

	private static final String GET =
		"SELECT " +
			"filter.pk_filter, "+
			"filter.pk_project, "+
			"filter.str_name,"+
			"filter.int_order,"+
			"filter.bool_enabled " +
		"FROM " +
			"plow.filter ";

    public static final RowMapper<FilterT> MAPPER = new RowMapper<FilterT>() {
        @Override
        public FilterT mapRow(ResultSet rs, int rowNum)
                throws SQLException {
        	FilterT filter = new FilterT();
        	filter.id = rs.getString("pk_filter");
        	filter.order = rs.getInt("int_order");
        	filter.name = rs.getString("str_name");
        	filter.enabled = rs.getBoolean("bool_enabled");
        	return filter;
        }
    };

	public List<FilterT> getAll(Project project) {
		return jdbc.query(GET + " WHERE pk_project=? ORDER BY int_order ASC",
				MAPPER, project.getProjectId());
	}

	@Override
	public FilterT get(UUID id) {
		return jdbc.queryForObject(GET + " WHERE pk_filter=?", MAPPER, id);
	}
}
