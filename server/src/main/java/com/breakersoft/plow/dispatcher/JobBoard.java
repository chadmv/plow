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

import com.breakersoft.plow.Project;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchableFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchableJob;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.FolderCreatedEvent;
import com.breakersoft.plow.event.JobFinishedEvent;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.event.ProcAllocatedEvent;
import com.breakersoft.plow.event.ProcDeallocatedEvent;
import com.breakersoft.plow.event.ProjectCreatedEvent;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

@Component
public class JobBoard {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(JobBoard.class);

    @Autowired
    EventManager eventManager;

    @Autowired
    DispatchService dispatchService;

    @Autowired
    ProjectService projectService;

    @Autowired
    JobService jobService;

    private Map<UUID, ArrayList<DispatchableJob>> activeJobs;
    private ConcurrentMap<UUID, DispatchableFolder> folderIndex;
    private ConcurrentMap<UUID, DispatchableJob> jobIndex;

    public JobBoard() {
        activeJobs = Maps.newHashMap();
        folderIndex = Maps.newConcurrentMap();
        jobIndex = Maps.newConcurrentMap();
    }

    @PostConstruct
    public void init() {

        // Pre-populate projects.
        for (Project project: projectService.getProjects()) {
            activeJobs.put(project.getProjectId(), new ArrayList<DispatchableJob>(32));
        }
        logger.info("Prepopulated {} projects.", activeJobs.size());

        // Prepopulte folders.
        for (DispatchableFolder folder: dispatchService.getDispatchFolders()) {
            folderIndex.put(folder.folderId, folder);
        }
        logger.info("Prepopulated {} folders.", folderIndex.size());

        // Propopulate jobs.
        for (DispatchableJob job: dispatchService.getDispatchJobs()) {
            jobIndex.put(job.jobId, job);
            activeJobs.get(job.getProjectId()).add(job);
        }
        logger.info("Prepopulated {} jobs.", jobIndex.size());

        eventManager.register(this);

        // Enable the dispatcher.
        DispatchConfig.IS_ENABLED.set(true);
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

        for (DispatchableJob job: activeJobs.get(project.getProjectId())) {

            // Job has no pending frames.
            if (!job.isDispatchable()) {
                continue;
            }

            if (!jobService.hasWaitingFrames(job)) {
                continue;
            }

            /*
            // Check tags
            if (Sets.intersection(node.getTags(),
                    job.tags).isEmpty()) {
                continue;
            }
            */

            result.add(job);
        }

        Collections.sort(result);
        return result;
    }

    @Subscribe
    public void handleProcBookedEvent(ProcAllocatedEvent event) {

        DispatchableJob job = jobIndex.get(event.proc.getJobId());
        if (job == null) {
            logger.info("Unable to handle job booked event, job was shutdown.");
            return;
        }
        DispatchableFolder folder = folderIndex.get(job.folderId);

        logger.info("Handling job booked event, before {} / {}",
                job.runCores, folder.runCores);

        DispatchProc proc = event.proc;
        job.incrementAndGetCores(proc.getIdleCores());
        folder.incrementAndGetCores(proc.getIdleCores());
    }

    @Subscribe
    public void handleProcUnbookedEvent(ProcDeallocatedEvent event) {

        logger.info("Proc unbooked event: " + event.proc.getProcId());

        DispatchableJob job = jobIndex.get(event.proc.getJobId());
        DispatchableFolder folder = folderIndex.get(job.folderId);

        job.incrementAndGetCores(event.proc.getIdleCores() * -1);
        folder.incrementAndGetCores(event.proc.getIdleCores() * -1);

        logger.info("Job {} at {} running cores.", job.jobId, job.runCores);

        if (job.runCores == 0) {
            logger.info("Removing JOB {} from dispatcher.", job.jobId);

            final UUID projId = job.getProjectId();
            synchronized (activeJobs.get(projId)) {
                activeJobs.get(projId).remove(job);
            }

            jobIndex.remove(job);
            logger.info("{} jobs in project {}", projId, activeJobs.get(projId).size());
        }
    }

    @Subscribe
    public void handleJobLaunchEvent(JobLaunchEvent event) {
        logger.info("Job launched event: " + event.getJob().getJobId());

        DispatchableJob job = dispatchService.getDispatchJob(event);
        job.folder = folderIndex.get(job.folderId);
        jobIndex.put(job.jobId, job);

        final UUID projId = event.getJob().getProjectId();
        synchronized (activeJobs.get(projId)) {
            activeJobs.get(projId).add(job);
        }

        logger.info("{} jobs in project {}", projId, activeJobs.get(projId).size());
    }

    @Subscribe
    public void handleProjectCreatedEvent(ProjectCreatedEvent event) {
        logger.info("Adding dispatch project: " + event.project.getProjectId());
        activeJobs.put(event.project.getProjectId(), new ArrayList<DispatchableJob>(32));
    }

    @Subscribe
    public void handleFolderCreatedEvent(FolderCreatedEvent event) {
        logger.info("Adding dispatch folder: " + event.folder.getFolderId());
        DispatchableFolder folder =
                dispatchService.getDispatchFolder(event.folder.getFolderId());
        folderIndex.put(folder.folderId, folder);
    }

    @Subscribe
    public void handleJobShutdownEvent(JobFinishedEvent event) {
        // Set the job's dispatchable boolean to false.
        logger.info("Job shutdown event " + event.getJob().getJobId());

        DispatchableJob job = jobIndex.get(event.getJob().getJobId());
        if (job == null) {
            return;
        }
        else {
            job.isDispatchable = false;
        }
    }
}
