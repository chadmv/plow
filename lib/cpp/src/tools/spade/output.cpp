#include "output.h"
#include "plow.h"

#include <iostream>

using namespace Plow;

namespace Spade {

void display_job_list()
{

    JobFilterT filter;
    filter.states.push_back(JobState::RUNNING);

    std::vector<JobT> jobs;
    getJobs(jobs, filter);

    for (std::vector<JobT>::iterator i = jobs.begin();
                                     i != jobs.end();
                                     ++i)
    {
        std::cout << i->name << std::endl;

    }
}

void display_task_list(const std::string& job_name)
{





}

} // namespace
