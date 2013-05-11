package com.breakersoft.plow.dispatcher.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Node;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;

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

    List<DispatchJob> getDispatchJobs(DispatchProject project, DispatchNode node);

    DispatchJob getDispatchJob(UUID id);

    List<DispatchProc> getOrphanProcs();
}
