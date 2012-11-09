
#include "plow.h"

#include <vector>
#include <string>

PLOW_NAMESPACE_ENTER

void getJobs(std::vector<JobT>& jobs, const JobFilterT& filter)
{
    PlowClient* client = getClient();
    client->proxy().getJobs(jobs, filter);
}

PLOW_NAMESPACE_EXIT