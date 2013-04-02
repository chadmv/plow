package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.Matcher;
import com.breakersoft.plow.MatcherE;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.MatcherDao;
import com.breakersoft.plow.thrift.MatcherField;
import com.breakersoft.plow.thrift.MatcherType;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class MatcherDaoImpl extends AbstractDao implements MatcherDao {

	private static final String INSERT =
		    JdbcUtils.Insert(
		    		"plow.matcher",
		            "pk_matcher",
		            "pk_filter",
		            "int_field",
		            "int_type",
		            "str_value");

	@Override
	public Matcher create(Filter filter, MatcherField field, MatcherType type, String value) {
		final UUID id = UUID.randomUUID();
		jdbc.update(INSERT, id, filter.getFilterId(), field.ordinal(), type.ordinal(), value);

		MatcherE matcher = new MatcherE();
		matcher.setFilterId(filter.getFilterId());
		matcher.setMatcherId(id);
		return matcher;
	}

    public static final RowMapper<Matcher> MAPPER = new RowMapper<Matcher>() {
        @Override
        public Matcher mapRow(ResultSet rs, int rowNum)
                throws SQLException {
        	MatcherE matcher = new MatcherE();
        	matcher.setMatcherId((UUID) rs.getObject("pk_matcher"));
        	matcher.setFilterId((UUID) rs.getObject("pk_filter"));
        	return matcher;
        }
    };

	private static final String GET =
			"SELECT " +
				"pk_filter, " +
				"pk_matcher " +
			"FROM " +
				"plow.matcher " +
			"WHERE " +
				"pk_matcher = ? ";

	@Override
	public Matcher get(UUID id) {
		return jdbc.queryForObject(GET, MAPPER, id);
	}

	@Override
	public boolean delete(Matcher matcher) {
		return jdbc.update("DELETE FROM matcher WHERE pk_matcher=?", matcher.getMatcherId()) == 1;
	}

	@Override
	public void setValue(Matcher matcher, String value) {
		jdbc.update("UPDATE matcher SET str_value=? WHERE pk_matcher=?", value, matcher.getMatcherId());
	}

	@Override
	public void setField(Matcher matcher, MatcherField field) {
		jdbc.update("UPDATE matcher SET int_field=? WHERE pk_matcher=?", field.ordinal(),
				matcher.getMatcherId());
	}

	@Override
	public void setType(Matcher matcher, MatcherType type) {
		jdbc.update("UPDATE matcher SET int_type=? WHERE pk_matcher=?", type.ordinal(),
				matcher.getMatcherId());
	}

	private static final String UPDATE =
		"UPDATE " +
			"matcher " +
		"SET " +
			"int_field=?,"+
			"int_type=?,"+
			"str_value=? "+
		"WHERE " +
			"pk_matcher=?";

	@Override
	public void update(Matcher matcher, MatcherField field, MatcherType type, String value) {
		jdbc.update(UPDATE, field.ordinal(), type.ordinal(), value, matcher.getMatcherId());
	}
}
