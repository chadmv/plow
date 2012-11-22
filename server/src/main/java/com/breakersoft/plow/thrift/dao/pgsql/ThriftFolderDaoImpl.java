package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.FolderT;
import com.breakersoft.plow.thrift.dao.ThriftFolderDao;

@Repository
public class ThriftFolderDaoImpl extends AbstractDao implements ThriftFolderDao {

    private static final String GET =
            "SELECT " +
                "folder.pk_folder,"+
                "folder.str_name, "+
                "folder.int_order " +
            "FROM " +
                "folder ";

    @Override
    public FolderT get(UUID id){
        return jdbc.queryForObject(GET + " WHERE pk_folder=?", new RowMapper<FolderT>() {
            @Override
            public FolderT mapRow(ResultSet rs, int rowNum) throws SQLException {
                FolderT folder = new FolderT();
                folder.setId(rs.getString("pk_folder"));
                folder.setOrder(rs.getInt("int_order"));
                folder.setName(rs.getString("str_name"));
                return folder;
            }
        }, id);
    }
}
