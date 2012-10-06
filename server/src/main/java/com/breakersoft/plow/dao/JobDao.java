package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.thrift.JobBp;
import com.breakersoft.plow.thrift.JobState;

public interface JobDao {

    Job create(Project project, JobBp blueprint);

    Job get(String name, JobState state);

    Job get(UUID id);

    void updateFrameStatesForLaunch(Job job);

    void updateFrameCountsForLaunch(Job job);

    boolean setJobState(Job job, JobState state);

    boolean hasWaitingFrames(Job job);

    boolean hasPendingFrames(Job job);
}
