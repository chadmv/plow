
#include "plow.h"
#include "client.h"

#include <vector>
#include <string>

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


PLOW_NAMESPACE_EXIT