package com.breakersoft.plow.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.FrameSet;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.FrameDao;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.dao.ProjectDao;
import com.breakersoft.plow.json.Blueprint;
import com.breakersoft.plow.json.BlueprintLayer;

@Service
@Transactional
public class JobLauncherServiceImpl implements JobLauncherService {

    @SuppressWarnings("unused")
    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(JobLauncherServiceImpl.class);

    @Autowired
    ProjectDao projectDao;

    @Autowired
    JobDao jobDao;

    @Autowired
    LayerDao layerDao;

    @Autowired
    FrameDao frameDao;

    public Job launch(Blueprint blueprint) {

        Job job = createJobTopology(blueprint);
        // TODO: setup dependencies.

        jobDao.updateFrameStatesForLaunch(job);
        jobDao.updateFrameCountsForLaunch(job);

        return job;
    }

    public void shutdown(Job job) {
        // TODO Auto-generated method stub

    }

    private Job createJobTopology(Blueprint blueprint) {

        Project project = projectDao.get(blueprint.getProject());
        Job job = jobDao.create(project, blueprint);

        int layerOrder = 0;
        for (BlueprintLayer blayer: blueprint.getLayers()) {
            Layer layer = layerDao.create(job, blayer, layerOrder);

            int frameOrder = 0;
            FrameSet frameSet = new FrameSet(blayer.getRange());
            for (int frameNum: frameSet) {
                frameDao.create(layer, frameNum, frameOrder);
                frameOrder++;
            }
            layerOrder++;
        }

        return job;
    }

}
