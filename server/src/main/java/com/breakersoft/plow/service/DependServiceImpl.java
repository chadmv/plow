package com.breakersoft.plow.service;

import static com.breakersoft.plow.util.PlowUtils.checkEmpty;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Depend;
import com.breakersoft.plow.FrameRange;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.TaskOnTaskBatch;
import com.breakersoft.plow.dao.DependDao;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.exceptions.DependencyException;
import com.breakersoft.plow.thrift.DependSpecT;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Service
@Transactional
public class DependServiceImpl implements DependService {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(DependServiceImpl.class);

    @Autowired
    JobDao jobDao;

    @Autowired
    LayerDao layerDao;

    @Autowired
    TaskDao taskDao;

    @Autowired
    DependDao dependDao;

    public Depend createDepend(Job job, DependSpecT spec) {
        spec.dependentJob = job.getName();
        spec.dependOnJob = job.getName();
        return createDepend(spec);
    }

    public Depend createDepend(DependSpecT spec) {

        Job dependentJob;
        Job dependOnJob;
        Layer dependentLayer;
        Layer dependOnLayer;
        Task dependentTask;
        Task dependOnTask;

        Depend depend = null;

        if (spec.getType() == null) {
            throw new DependencyException("Dependency type is not set to a value.");
        }

        switch(spec.getType()) {

        case JOB_ON_JOB:
            dependentJob = jobDao.getByActiveNameOrId(checkEmpty(spec.dependentJob));
            dependOnJob = jobDao.getByActiveNameOrId(checkEmpty(spec.dependOnJob));
            depend = dependDao.createJobOnJob(dependentJob, dependOnJob);
            break;

        case LAYER_ON_LAYER:
            dependentJob = jobDao.getByActiveNameOrId(checkEmpty(spec.dependentJob));
            dependOnJob = jobDao.getByActiveNameOrId(checkEmpty(spec.dependOnJob));
            dependentLayer = layerDao.get(dependentJob,checkEmpty(spec.dependentLayer));
            dependOnLayer = layerDao.get(dependOnJob,checkEmpty(spec.dependOnLayer));
            depend = dependDao.createLayerOnLayer(
                    dependentJob, dependentLayer, dependentJob, dependOnLayer);
            break;

        case LAYER_ON_TASK:
            dependentJob = jobDao.getByActiveNameOrId(checkEmpty(spec.dependentJob));
            dependentLayer = layerDao.get(dependentJob, checkEmpty(spec.dependentLayer));
            dependOnJob = jobDao.getByActiveNameOrId(checkEmpty(spec.dependOnJob));
            dependOnTask = taskDao.getByNameOrId(dependOnJob, checkEmpty(spec.dependOnTask));
            dependOnLayer = layerDao.get(dependOnTask.getLayerId());
            depend = dependDao.createLayerOnTask(dependentJob, dependentLayer,
                    dependOnJob, dependOnLayer, dependOnTask);
            break;

        case TASK_ON_LAYER:
            dependentJob = jobDao.getByActiveNameOrId(checkEmpty(spec.dependentJob));
            dependentTask = taskDao.getByNameOrId(dependentJob, checkEmpty(spec.dependentTask));
            dependentLayer = layerDao.get(dependentTask.getLayerId());
            dependOnJob = jobDao.getByActiveNameOrId(checkEmpty(spec.dependOnJob));
            dependOnLayer = layerDao.get(dependOnJob, checkEmpty(spec.dependOnLayer));
            depend = dependDao.createTaskOnLayer(
                    dependentJob, dependentLayer, dependentTask, dependOnJob, dependOnLayer);
            break;

        case TASK_ON_TASK:
            dependentJob = jobDao.getByActiveNameOrId(checkEmpty(spec.dependentJob));
            dependentTask = taskDao.getByNameOrId(dependentJob,checkEmpty(spec.dependentTask));
            dependentLayer = layerDao.get(dependentTask.getLayerId());
            dependOnJob = jobDao.getByActiveNameOrId(checkEmpty(spec.dependOnJob));
            dependOnTask = taskDao.getByNameOrId(dependOnJob,checkEmpty(spec.dependOnTask));
            dependOnLayer = layerDao.get(dependOnTask.getLayerId());
            depend = dependDao.createTaskOnTask(dependentJob, dependentLayer, dependentTask,
                    dependOnJob, dependOnLayer, dependOnTask);
            break;

        case TASK_BY_TASK:
            dependentJob = jobDao.getByActiveNameOrId(
                    checkEmpty(spec.dependentJob));
            dependOnJob = jobDao.getByActiveNameOrId(
                    checkEmpty(spec.dependOnJob));
            dependentLayer = layerDao.get(dependentJob,
                    checkEmpty(spec.dependentLayer));
            dependOnLayer = layerDao.get(dependOnJob,
                    checkEmpty(spec.dependOnLayer));

            createTaskByTask(dependentJob, dependentLayer, dependOnJob, dependOnLayer);
            break;

        default:
            throw new DependencyException("Unhandled dependency type " + spec.getType());
        }

        if (depend != null) {
            dependDao.incrementDependCounts(depend);
        }
        return depend;
    }

    @Override
    public boolean satisfyDepend(Depend depend) {
        if (dependDao.satisfyDepend(depend)) {
            dependDao.decrementDependCounts(depend);
            return true;
        }
        return false;
    }

    @Override
    public boolean unsatisfyDepend(Depend depend) {
        if (dependDao.unsatisfyDepend(depend)) {
            dependDao.incrementDependCounts(depend);
            return true;
        }
        return false;
    }


    @Override
    @Transactional(readOnly=true)
    public Depend getDepend(UUID id) {
        return dependDao.get(id);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Depend> getOnJobDepends(Job job) {
        return dependDao.getOnJobDepends(job);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Depend> getOnLayerDepends(Layer layer) {
        return dependDao.getOnLayerDepends(layer);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Depend> getOnTaskDepends(Task task) {
        return dependDao.getOnTaskDepends(task);
    }

    private void createTaskByTask(Job dependentJob, Layer dependentLayer, Job dependOnJob, Layer dependOnLayer) {

        /*
         * Task by task depends requires both layers to actually
         * have a frame range.
         */
        final FrameRange dependentRange = layerDao.getFrameRange(dependentLayer);
        final FrameRange dependOnRange = layerDao.getFrameRange(dependOnLayer);
        final Set<Integer> dependOnTaskNumbers = Sets.newLinkedHashSet();

        final Map<Integer, UUID> dependentTaskCache = taskDao.buildTaskCache(dependentLayer, dependentRange.numFrames);
        final Map<Integer, UUID> dependOnTaskCache = taskDao.buildTaskCache(dependOnLayer, dependOnRange.numFrames);

        final TaskOnTaskBatch batch = new TaskOnTaskBatch(
                dependentRange.frameSet.size() / dependentRange.chunkSize);
        batch.dependentJob = dependentJob;
        batch.dependentLayer = dependentLayer;
        batch.dependOnJob = dependOnJob;
        batch.dependOnLayer = dependOnLayer;

        for (int i=0; i<dependentRange.frameSet.size(); i=i+dependentRange.chunkSize) {
            dependOnTaskNumbers.clear();

            // get the task number for the given index.
            int depTaskNum = dependentRange.frameSet.get(i);

            for (int c=0; c<dependentRange.chunkSize; c++) {
                int onTaskNum = depTaskNum + c;

                if (dependOnRange.frameSet.contains(onTaskNum)) {
                    if (dependOnRange.chunkSize == 1) {
                        dependOnTaskNumbers.add(onTaskNum);
                    }
                    else if (dependOnRange.chunkSize > 1) {
                        int idx = (dependOnRange.frameSet.indexOf(onTaskNum)
                                / dependOnRange.chunkSize) * dependOnRange.chunkSize;
                        dependOnTaskNumbers.add(dependOnRange.frameSet.get(idx));
                    }
                    else if (dependOnRange.chunkSize <=0 ) {
                        dependOnTaskNumbers.add(dependOnRange.frameSet.get(0));
                    }
                }
            }

            if (dependOnTaskNumbers.isEmpty()) {
                continue;
            }

            final UUID[] dependOns = new UUID[dependOnTaskNumbers.size()];
            final int[] dependOnNumbers = new int[dependOnTaskNumbers.size()];

            int index = 0;
            for (int taskNum: dependOnTaskNumbers) {
                final UUID dependOnId = dependOnTaskCache.get(taskNum);
                if (dependOnId == null) {
                    throw new DependencyException("Unable to find task " +
                            "" + taskNum + " in " + dependOnLayer.getName() + ", " +
                                    "while setting up dependencies.");
                }
                dependOns[index] = dependOnId;
                dependOnNumbers[index] = taskNum;
                index++;
            }

            batch.addEntry(dependentTaskCache.get(depTaskNum), depTaskNum, dependOns, dependOnNumbers);
        }

        dependDao.batchCreateTaskOnTask(batch);
        dependDao.batchIncrementDependCounts(batch);
    }
}
