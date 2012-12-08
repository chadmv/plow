package com.breakersoft.plow.dispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchableFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchableJob;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobFinishedEvent;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.event.ProcBookedEvent;
import com.breakersoft.plow.event.ProcUnbookedEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

@Component
public class JobBoard {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(JobBoard.class);

    @Autowired
    EventManager eventManager;


    private Map<UUID, ArrayList<DispatchableJob>> activeJobs;
    private ConcurrentMap<UUID, DispatchableFolder> folderIndex;
    private ConcurrentMap<UUID, DispatchableJob> jobIndex;

    @PostConstruct
    public void init() {
        eventManager.register(this);
    }

    public DispatchableJob getDispatchableJob(UUID id) {
        return jobIndex.get(id);
    }
    /**
     * Return the sorted job list that can taken the given node.
     *
     * @param node
     * @param project
     * @param limit
     */
    public List<DispatchableJob> getDispatchableJobs(DispatchNode node, DispatchProject project) {

        final int count = activeJobs.get(project.getProjectId()).size();
        final List<DispatchableJob> result = Lists.newArrayListWithExpectedSize(count);

        for (DispatchableJob job: activeJobs.get(project)) {

            // Job has no pending frames.
            if (!job.isDispatchable) {
                continue;
            }

            // Check tags
            if (Sets.intersection(node.getTags(),
                    job.tags).isEmpty()) {
                continue;
            }

            result.add(job);
        }

        Collections.sort(result);
        return result;
    }

    @Subscribe
    public void handleProcBookedEvent(ProcBookedEvent event) {

        DispatchableJob job = jobIndex.get(event.getTask().getJobId());
        if (job == null) {
            logger.info("Unable to handle job booked event, job was shutdown.");
            return;
        }
        DispatchableFolder folder = folderIndex.get(job.folderId);

        logger.info("Handling job booked event, before {} / {}",
                job.runCores, folder.runCores);

        DispatchProc proc = event.getProc();
        job.incrementAndGetCores(proc.getIdleCores());
        folder.incrementAndGetCores(proc.getIdleCores());
    }

    @Subscribe
    public void handleProcUnbookedEvent(ProcUnbookedEvent event) {

        DispatchableJob job = jobIndex.get(event.getProc().getJobId());
        DispatchableFolder folder = folderIndex.get(job.folderId);

        job.incrementAndGetCores(event.getProc().getIdleCores() * -1);
        folder.incrementAndGetCores(event.getProc().getIdleCores() * -1);

        if (job.runCores == 0) {
            logger.info("Removing JOB {} from dispatcher.", job.jobId);
            jobIndex.remove(job.jobId);
        }
    }

    @Subscribe
    public void handleJobLaunchEvent(JobLaunchEvent event) {
        DispatchableJob job = jobIndex.get(event.getJob().getJobId());
        job.folder = folderIndex.get(job.folderId);

        jobIndex.put(job.jobId, job);
        activeJobs.get(event.getJob().getProjectId()).add(job);
    }

    @Subscribe
    public void handleJobShutdownEvent(JobFinishedEvent event) {
        // Set the job's dispatchable boolean to false.
        jobIndex.get(event.getJob().getJobId()).isDispatchable = false;
    }
}
