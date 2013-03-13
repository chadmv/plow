package com.breakersoft.plow.dispatcher;

import java.util.List;

import com.breakersoft.plow.JobId;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResource;
import com.breakersoft.plow.dispatcher.domain.DispatchableTask;
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

    void unassignProc(DispatchProc proc);

    boolean stopTask(Task task, TaskState state);

    // New stuff.

    List<DispatchableTask> getDispatchableTasks(JobId job, DispatchResource resource);

    void deallocateProc(DispatchProc proc, String why);

    DispatchProc allocateProc(DispatchNode node, DispatchableTask task);

    RunTaskCommand getRuntaskCommand(Task task);

    boolean startTask(String hostname, DispatchableTask task);

    void assignProc(DispatchProc proc, DispatchableTask task);

    List<DispatchJob> getDispatchJobs(DispatchProject project, DispatchNode node);
}
