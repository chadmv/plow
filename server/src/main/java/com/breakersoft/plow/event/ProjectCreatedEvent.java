package com.breakersoft.plow.event;

import com.breakersoft.plow.Project;

public class ProjectCreatedEvent implements Event {

    public Project project;

    public ProjectCreatedEvent(Project project) {
        this.project = project;
    }
}
