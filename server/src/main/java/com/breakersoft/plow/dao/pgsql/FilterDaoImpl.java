package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.FilterE;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.FilterDao;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class FilterDaoImpl extends AbstractDao implements FilterDao {

    private static final String INSERT =
        JdbcUtils.Insert(
                "plow.filter",
                "pk_filter",
                "pk_project",
                "str_name",
                "int_order",
                "bool_enabled");

    @Override
    public Filter create(Project project, String name) {
        final UUID id = UUID.randomUUID();
        jdbc.update(INSERT, id, project.getProjectId(), name, -1, true);

        FilterE filter = new FilterE();
        filter.setFilterId(id);
        return filter;
    }

    public static final RowMapper<Filter> MAPPER = new RowMapper<Filter>() {
        @Override
        public Filter mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            FilterE filter = new FilterE();
            filter.setFilterId((UUID) rs.getObject(1));
            return filter;
        }
    };

    private static final String GET =
        "SELECT " +
            "pk_filter " +
        "FROM " +
            "plow.filter " +
        "WHERE " +
            "pk_project = ? " +
        "AND " +
            "int_order::integer = ?";

    @Override
    public Filter get(Project project, int order) {
        return jdbc.queryForObject(GET, MAPPER, project.getProjectId(), order);
    }

    private static final String GET_BY_ID =
        "SELECT " +
            "pk_filter " +
        "FROM " +
            "plow.filter " +
        "WHERE " +
            "pk_filter = ?";

    @Override
    public Filter get(UUID id) {
        return jdbc.queryForObject(GET_BY_ID, MAPPER, id);
    }

    @Override
    public void setName(Filter filter, String name) {
        jdbc.update("UPDATE plow.filter SET str_name=? WHERE pk_filter=?", name, filter.getFilterId());
    }

    @Override
    public boolean delete(Filter filter) {
        return jdbc.update(
                "DELETE FROM plow.filter WHERE pk_filter=?", filter.getFilterId()) == 1;
    }

    @Override
    public void reorder(Project project) {
        jdbc.batchUpdate("UPDATE plow.filter SET int_order=? WHERE pk_filter=?",
                jdbc.query("SELECT row_number() OVER (ORDER BY int_order ASC), pk_filter FROM plow.filter WHERE pk_project=?",
                        JdbcUtils.OBJECT_ARRAY_MAPPER, project.getProjectId()));
    }

    @Override
    public void setOrder(Filter filter, int order) {
        float _order = order + .5f;
        jdbc.update("UPDATE plow.filter SET int_order=? WHERE pk_filter=?", _order, filter.getFilterId());
    }

    @Override
    public void increaseOrder(Filter filter) {
        jdbc.update("UPDATE plow.filter SET int_order=int_order+1.5 WHERE pk_filter=?", filter.getFilterId());
    }

    @Override
    public void decreaseOrder(Filter filter) {
        jdbc.update("UPDATE plow.filter SET int_order=int_order-1.5 WHERE pk_filter=?", filter.getFilterId());
    }
}
