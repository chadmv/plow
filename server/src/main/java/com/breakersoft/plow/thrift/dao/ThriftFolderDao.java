package com.breakersoft.plow.thrift.dao;

import java.util.UUID;

import com.breakersoft.plow.thrift.FolderT;

public interface ThriftFolderDao {

    FolderT get(UUID id);

}
