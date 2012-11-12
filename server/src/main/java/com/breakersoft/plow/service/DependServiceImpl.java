package com.breakersoft.plow.service;

import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Depend;
import com.breakersoft.plow.FrameRange;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.DependDao;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.thrift.DependSpecT;
import com.google.common.collect.Sets;

@Service
@Transactional
public class DependServiceImpl implements DependService {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(DependServiceImpl.class);

    @Autowired
    JobDao jobDao;

    @Autowired
    LayerDao layerDao;

    @Autowired
    TaskDao taskDao;

    @Autowired
    DependDao dependDao;

    public Depend createDepend(DependSpecT spec) {

        Job dependentJob;
        Job dependOnJob;
        Layer dependentLayer;
        Layer dependOnLayer;
        Task dependentTask;
        Task dependOnTask;

        Depend depend = null;

        switch(spec.getType()) {

        case JOB_ON_JOB:
            dependentJob = jobDao.getByActiveNameOrId(spec.dependentJob);
            dependOnJob = jobDao.getByActiveNameOrId(spec.dependOnJob);
            depend = dependDao.createJobOnJob(dependentJob, dependOnJob);
            break;

        case LAYER_ON_LAYER:
            dependentJob = jobDao.getByActiveNameOrId(spec.dependentJob);
            dependOnJob = jobDao.getByActiveNameOrId(spec.dependOnJob);
            dependentLayer = layerDao.get(dependentJob, spec.dependentLayer);
            dependOnLayer = layerDao.get(dependOnJob, spec.dependOnLayer);
            depend = dependDao.createLayerOnLayer(dependentLayer, dependOnLayer);
            break;

        case LAYER_ON_TASK:
            dependentJob = jobDao.getByActiveNameOrId(spec.dependentJob);
            dependOnJob = jobDao.getByActiveNameOrId(spec.dependOnJob);
            dependentLayer = layerDao.get(dependentJob, spec.dependentLayer);
            dependOnLayer = layerDao.get(dependOnJob, spec.dependOnLayer);
            dependOnTask = taskDao.getByNameOrId(dependOnLayer, spec.dependOnTask);
            depend = dependDao.createLayerOnTask(dependentLayer, dependOnTask);
            break;

        case TASK_ON_LAYER:
            dependentJob = jobDao.getByActiveNameOrId(spec.dependentJob);
            dependOnJob = jobDao.getByActiveNameOrId(spec.dependOnJob);
            dependentLayer = layerDao.get(dependentJob, spec.dependentLayer);
            dependOnLayer = layerDao.get(dependOnJob, spec.dependOnLayer);
            dependentTask = taskDao.getByNameOrId(dependentLayer, spec.dependentTask);
            depend = dependDao.createTaskOnLayer(dependentTask, dependOnLayer);
            break;

        case TASK_ON_TASK:
            dependentJob = jobDao.getByActiveNameOrId(spec.dependentJob);
            dependOnJob = jobDao.getByActiveNameOrId(spec.dependOnJob);
            dependentLayer = layerDao.get(dependentJob, spec.dependentLayer);
            dependOnLayer = layerDao.get(dependOnJob, spec.dependOnLayer);
            dependentTask = taskDao.getByNameOrId(dependentLayer, spec.dependentTask);
            dependOnTask = taskDao.getByNameOrId(dependOnLayer, spec.dependOnTask);
            depend = dependDao.createTaskOnTask(dependentTask, dependOnTask);
            break;

        case TASK_BY_TASK:
            dependentJob = jobDao.getByActiveNameOrId(spec.dependentJob);
            dependOnJob = jobDao.getByActiveNameOrId(spec.dependOnJob);
            dependentLayer = layerDao.get(dependentJob, spec.dependentLayer);
            dependOnLayer = layerDao.get(dependOnJob, spec.dependOnLayer);
            createTaskByTask(dependentLayer, dependOnLayer);
            break;
        }

        if (depend != null) {
            dependDao.incrementDependCounts(depend);
        }
        return depend;
    }

    @Override
    public void satisfyDependsOn(Task task) {
        for (Depend depend: dependDao.getOnTaskDepends(task)) {
            if (dependDao.satisfyDepend(depend)) {
                dependDao.decrementDependCounts(depend);
            }
        }
    }

    private void createTaskByTask(Layer dependentLayer, Layer dependOnLayer) {

        /*
         * Task by task depends requires both layers to actually
         * have a frame range.
         */
        FrameRange dependentRange = layerDao.getFrameRange(dependentLayer);
        FrameRange dependOnRange = layerDao.getFrameRange(dependOnLayer);

        for (int i=0; i<dependentRange.numFrames;i++) {

            Set<Integer> dependOnTaskNumbers = Sets.newLinkedHashSet();

            int depTaskNum = dependentRange.frameSet.get(i);
            int onTaskNum = depTaskNum;

            if (dependOnRange.frameSet.contains(onTaskNum)) {
                dependOnTaskNumbers.add(onTaskNum);
            }

            //TODO: handle chunking.
            //TODO: handle the care where the layer is chunked to 1 frame.

            if (dependOnTaskNumbers.isEmpty()) {
                continue;
            }

            Task dependentTask = taskDao.get(dependentLayer, depTaskNum);
            for (int taskNum: dependOnTaskNumbers) {
                Task dependOnTask = taskDao.get(dependOnLayer, taskNum);
                Depend dep = dependDao.createTaskOnTask(dependentTask, dependOnTask);
                dependDao.incrementDependCounts(dep);
            }
        }
    }
}
