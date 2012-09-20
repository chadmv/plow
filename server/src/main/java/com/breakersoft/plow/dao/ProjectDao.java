package com.breakersoft.plow.dao;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Project;

public interface ProjectDao {

    /**
     *
     * @param name
     * @param title
     * @return
     */
    Project create(String name, String title);

    /**
     *
     * @param name
     * @return
     */
    Project get(String name);

    /**
     * Set the project's default folder. This is where
     * unfiltered jobs go.
     *
     * @param project
     * @param folder
     */
    void setDefaultFolder(Project project, Folder folder);


}
