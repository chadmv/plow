package com.breakersoft.plow.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.domain.DispatchFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResource;
import com.breakersoft.plow.dispatcher.domain.DispatchableFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchableJob;
import com.breakersoft.plow.dispatcher.domain.DispatchableTask;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;

/**
 * A DAO for obtaining Dispatch brand objects.
 *
 * @author chambers
 *
 */
public interface DispatchDao {

    List<DispatchProject> getSortedProjectList(Node node);

    DispatchNode getDispatchNode(String name);

    DispatchProc getDispatchProc(UUID id);

    List<DispatchableJob> getDispatchableJobs();

    DispatchableFolder getDispatchableFolder(UUID folder);

    List<DispatchableTask> getDispatchableTasks(UUID jobId,
            DispatchResource resource);

    void decrementDispatchTotals(DispatchProc proc);

    void incrementDispatchTotals(DispatchProc proc);

    RunTaskCommand getRunTaskCommand(Task task);

    DispatchableJob getDispatchableJob(Job job);

    List<DispatchableFolder> getDispatchableFolders();
}
