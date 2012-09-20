package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Project;

public interface FolderDao {

    Folder createFolder(Project project, String name);

    Folder get(UUID id);

    Folder getDefaultFolder(Project project);

}
