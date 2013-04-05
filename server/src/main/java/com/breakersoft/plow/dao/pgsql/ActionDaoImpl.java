package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Action;
import com.breakersoft.plow.ActionE;
import com.breakersoft.plow.Filter;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.ActionDao;
import com.breakersoft.plow.thrift.ActionType;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class ActionDaoImpl extends AbstractDao implements ActionDao {

	private static final String INSERT =
		    JdbcUtils.Insert(
		    		"plow.action",
		            "pk_action",
		            "pk_filter",
		            "int_type",
		            "str_value");

	@Override
	public Action create(Filter filter, ActionType type, String value) {
		UUID id = UUID.randomUUID();
		jdbc.update(INSERT, id, filter.getFilterId(), type.ordinal(), value);

		ActionE action = new ActionE();
		action.setActionId(id);
		action.setFilterId(id);
		return action;
	}

    public static final RowMapper<Action> MAPPER = new RowMapper<Action>() {
        @Override
        public Action mapRow(ResultSet rs, int rowNum)
                throws SQLException {
        	ActionE action = new ActionE();
        	action.setActionId((UUID) rs.getObject("pk_action"));
        	action.setFilterId((UUID) rs.getObject("pk_filter"));
        	return action;
        }
    };

    private static final String GET =
    	"SELECT " +
    		"action.pk_action,"+
    		"action.pk_filter " +
    	"FROM " +
    		"plow.action " +
    	"WHERE " +
    		"action.pk_action=?";

	@Override
	public Action get(UUID id) {
		return jdbc.queryForObject(GET, MAPPER, id);
	}

	@Override
	public void delete(Action action) {
		jdbc.update("DELETE FROM plow.action WHERE pk_action=?", action.getActionId());
	}
}
