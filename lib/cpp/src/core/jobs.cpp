#include <vector>
#include <string>
#include <sstream>
#include <cstdlib>

#include "plow/plow.h"
#include "client.h"

PLOW_NAMESPACE_ENTER

void getJobs(std::vector<JobT>& jobs, const JobFilterT& filter)
{
    PlowClient* client = getClient();
    client->proxy().getJobs(jobs, filter);
}

void getActiveJob(JobT& job, const std::string& name)
{
    PlowClient* client = getClient();
    client->proxy().getActiveJob(job, name);
}

void getTasks(std::vector<TaskT>& tasks, const TaskFilterT& filter)
{
    PlowClient* client = getClient();
    client->proxy().getTasks(tasks, filter);
}

void killJob(const JobT& job)
{
    std::stringstream ss;
    ss << "Manually killed by UID: " << getuid();

    getClient()->proxy().killJob(job.id, ss.str()); 
}


PLOW_NAMESPACE_EXIT