package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.thrift.FolderT;

public interface ThriftJobBoardDao {

    List<FolderT> getJobBoard(UUID projectId);

}
