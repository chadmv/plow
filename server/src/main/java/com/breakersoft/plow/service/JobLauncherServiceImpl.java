package com.breakersoft.plow.service;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.FrameSet;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.dao.ProjectDao;
import com.breakersoft.plow.dispatcher.DispatchLayer;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.json.Blueprint;
import com.breakersoft.plow.json.BlueprintLayer;
import com.breakersoft.plow.thrift.JobState;
import com.google.common.collect.Lists;

@Service
@Transactional
public class JobLauncherServiceImpl implements JobLauncherService {

    @SuppressWarnings("unused")
    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(JobLauncherServiceImpl.class);

    @Autowired
    ProjectDao projectDao;

    @Autowired
    FolderDao folderDao;

    @Autowired
    JobDao jobDao;

    @Autowired
    LayerDao layerDao;

    @Autowired
    TaskDao frameDao;

    @Autowired
    EventManager eventManager;

    public JobLaunchEvent launch(Blueprint blueprint) {

        final Project project = projectDao.get(blueprint.getProject());
        final Job job = jobDao.create(project, blueprint);
        final Folder folder = filterJob(job, project);

        createJobTopology(job, project, blueprint);

        jobDao.updateFrameStatesForLaunch(job);
        jobDao.updateFrameCountsForLaunch(job);
        jobDao.setJobState(job, JobState.RUNNING);

        //TODO: do this someplace else. (tranny hook or aspect)
        // Don't want to add jobs to the dispatcher that fail to
        // commit to the DB.
        JobLaunchEvent event = new JobLaunchEvent(job, folder, blueprint);
        eventManager.post(event);

        return event;
    }

    public void shutdown(Job job) {
        // TODO Auto-generated method stub

    }

    private Folder filterJob(Job job, Project project) {
        // TODO: fully implement
        return folderDao.getDefaultFolder(project);
    }

    private void createJobTopology(
            Job job, Project project, Blueprint blueprint) {

        int layerOrder = 0;
        for (BlueprintLayer blayer: blueprint.getLayers()) {
            Layer layer = layerDao.create(job, blayer, layerOrder);

            int frameOrder = 0;
            FrameSet frameSet = new FrameSet(blayer.getRange());
            for (int frameNum: frameSet) {
                frameDao.create(layer, frameNum, frameOrder, layerOrder);
                frameOrder++;
            }
            layerOrder++;
        }
    }
}
