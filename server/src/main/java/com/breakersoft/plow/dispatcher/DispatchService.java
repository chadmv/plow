package com.breakersoft.plow.dispatcher;

import java.util.List;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.JobId;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResource;
import com.breakersoft.plow.dispatcher.domain.DispatchResult;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
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

    DispatchProc getDispatchProc(String id);

    DispatchProc getDispatchProc(Task task);

    /**
     * Unassigns the task from the proc. The proc is still assigned to
     * the job, and folder/job/layer counts are not changed.
     *
     * @param proc
     */
    void unassignProc(DispatchProc proc);

    boolean stopTask(Task task, TaskState state, int exitStatus, int exitSignal);

    // New stuff.

    List<DispatchTask> getDispatchableTasks(JobId job, DispatchResource resource, int limit);

    /**
     * Removes the proc from the proc table and updates proc counts.
     *
     * @param proc
     * @param why
     */
    void deallocateProc(Proc proc, String why);

    DispatchProc allocateProc(DispatchNode node, DispatchTask task);

    List<DispatchProc> getOrphanProcs();

    RunTaskCommand getRuntaskCommand(Task task);

    boolean startTask(DispatchTask task, DispatchProc proc);

    void assignProc(DispatchProc proc, DispatchTask task);

    List<DispatchJob> getDispatchJobs(DispatchProject project, DispatchNode node);

    boolean isAtMaxRetries(Task task);

    boolean quotaCheck(Cluster cluster, Project project);

    boolean dependQueueProcessed(Task task);

    List<DispatchTask> getDispatchableTasks(JobId job, DispatchResource resource);

    void setProcDeallocated(Proc proc);
}
