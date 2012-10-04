package com.breakersoft.plow.event;

import java.util.List;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.dispatcher.domain.DispatchLayer;
import com.breakersoft.plow.json.Blueprint;

public class JobLaunchEvent implements Event {

    private final Job job;
    private final Folder folder;
    private final Blueprint blueprint;


    public JobLaunchEvent(Job job, Folder folder, Blueprint blueprint) {
        this.job = job;
        this.folder = folder;
        this.blueprint = blueprint;
    }

    public Job getJob() {
        return job;
    }

    public Blueprint getBlueprint() {
        return blueprint;
    }

    public Folder getFolder() {
        return folder;
    }
}
