package com.breakersoft.plow.service;

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

    @Autowired
    ProjectDao projectDao;

    @Autowired
    JobDao jobDao;

    @Autowired
    LayerDao layerDao;

    @Autowired
    FrameDao frameDao;

    public Job launch(Blueprint blueprint) {

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

    public void shutdown(Job job) {
        // TODO Auto-generated method stub

    }

}
