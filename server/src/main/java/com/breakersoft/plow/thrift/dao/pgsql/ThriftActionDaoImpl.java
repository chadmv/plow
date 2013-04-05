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
import com.breakersoft.plow.thrift.ActionT;
import com.breakersoft.plow.thrift.ActionType;
import com.breakersoft.plow.thrift.dao.ThriftActionDao;

@Repository
@Transactional(readOnly=true)
public class ThriftActionDaoImpl extends AbstractDao implements ThriftActionDao {


	/*
	 * struct ActionT {
    1:common.Guid id,
    2:ActionType type,
    3:optional string value
}
	 */
    public static final RowMapper<ActionT> MAPPER = new RowMapper<ActionT>() {

        @Override
        public ActionT mapRow(ResultSet rs, int rowNum)
                throws SQLException {
        	final ActionT action = new ActionT(
        			rs.getString(1),
        			ActionType.findByValue(rs.getInt(2)));
        	action.setValue(rs.getString(3));
        	return action;
        }
    };

	private static final String GET =
			"SELECT " +
				"action.pk_action,"+
				"action.int_type,"+
				"action.str_value " +
			"FROM " +
				"plow.action ";

	@Override
	public ActionT get(UUID id) {
		return jdbc.queryForObject(GET + " WHERE pk_action=?", MAPPER, id);
	}

	@Override
	public List<ActionT> getAll(Filter filter) {
		return jdbc.query(GET + " WHERE pk_filter=?", MAPPER, filter.getFilterId());
	}
}
