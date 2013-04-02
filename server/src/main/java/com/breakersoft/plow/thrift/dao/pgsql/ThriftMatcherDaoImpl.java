package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.MatcherField;
import com.breakersoft.plow.thrift.MatcherT;
import com.breakersoft.plow.thrift.MatcherType;
import com.breakersoft.plow.thrift.dao.ThriftMatcherDao;

@Repository
@Transactional(readOnly=true)
public class ThriftMatcherDaoImpl extends AbstractDao implements ThriftMatcherDao {

	private static final String GET =
		"SELECT " +
			"matcher.pk_matcher, "+
			"matcher.int_field,"+
			"matcher.int_type,"+
			"matcher.int_order,"+
			"matcher.str_value " +
		"FROM " +
			"plow.matcher ";

	public static final RowMapper<MatcherT> MAPPER = new RowMapper<MatcherT>() {
		@Override
		public MatcherT mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			MatcherT matcher = new MatcherT();
			matcher.id = rs.getString("pk_matcher");
			matcher.field = MatcherField.findByValue(rs.getInt("int_field"));
			matcher.type = MatcherType.findByValue(rs.getInt("int_type"));
			matcher.value = rs.getString("str_value");
			return matcher;
		}
	};

	@Override
	public List<MatcherT> getAll(Filter filter) {
		return jdbc.query(GET + " WHERE pk_filter=? ORDER BY int_order ASC",
				MAPPER, filter.getFilterId());
	}

	@Override
	public MatcherT get(UUID id) {
		return jdbc.queryForObject(GET + " WHERE pk_matcher=?", MAPPER, id);
	}
}
