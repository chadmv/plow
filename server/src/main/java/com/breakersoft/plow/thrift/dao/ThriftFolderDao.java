package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Project;
import com.breakersoft.plow.thrift.FolderT;

public interface ThriftFolderDao {

    FolderT get(UUID id);

    List<FolderT> getFolders(Project project);

}
