package com.breakersoft.plow.dao;

import java.util.Map;
import java.util.UUID;

import com.breakersoft.plow.FilterableJob;
import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.JobState;

public interface JobDao {

    FilterableJob create(Project project, JobSpecT blueprint);

    Job get(String name, JobState state);

    Job get(UUID id);

    void updateFrameStatesForLaunch(Job job);

    void updateFrameCountsForLaunch(Job job);

    boolean setJobState(Job job, JobState state);

    boolean hasWaitingFrames(Job job);

    boolean isFinished(Job job);

    void updateFolder(Job job, Folder folder);

    boolean shutdown(Job job);

    Job getActive(String name);

    Job getByActiveNameOrId(String identifer);

    Job getActive(UUID id);

    void setPaused(Job job, boolean value);

    boolean isPaused(Job job);

    void setMaxCores(Job job, int value);

    void setMinCores(Job job, int value);

    void setAttrs(Job job, Map<String, String> attrs);

    Map<String, String> getAttrs(Job job);
}
