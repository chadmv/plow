package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResource;
import com.breakersoft.plow.dispatcher.domain.DispatchableFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchableJob;
import com.breakersoft.plow.dispatcher.domain.DispatchableTask;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;
import com.breakersoft.plow.thrift.TaskState;

/**
 * Transactional dispatcher opterations.
 *
 * @author chambers
 *
 */
public interface DispatchService {

    List<DispatchProject> getSortedProjectList(DispatchNode node);

    DispatchNode getDispatchNode(String name);

    boolean reserveTask(Task task);

    boolean unreserveTask(Task task);

    DispatchableJob getDispatchJob(JobLaunchEvent event);

    List<DispatchableJob> getDispatchJobs();

    DispatchProc getDispatchProc(String id);

    void unassignProc(DispatchProc proc);

    DispatchableFolder getDispatchFolder(UUID folder);

    boolean stopTask(Task task, TaskState state);

    // New stuff.

    List<DispatchableTask> getDispatchableTasks(final UUID jobId, final DispatchResource resource);

    void deallocateProc(DispatchProc proc, String why);

    DispatchProc allocateProc(DispatchNode node, DispatchableTask task);

    RunTaskCommand getRuntaskCommand(Task task);

    boolean startTask(String hostname, DispatchableTask task);

    void assignProc(DispatchProc proc, DispatchableTask task);

    List<DispatchableFolder> getDispatchFolders();




}
