#include "output.h"
#include "plow.h"

#include <iostream>
#include <iomanip>
#include <map>

using namespace Plow;

namespace Spade {

void display_node_list()
{
    NodeFilterT filter;

    std::vector<NodeT> nodes;
    getNodes(nodes, filter);

    for (std::vector<NodeT>::iterator i = nodes.begin();
                                      i != nodes.end();
                                      ++i)
    {
        std::cout << std::left << std::setw(32) << i->name << std::endl; 
    }
}

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
    JobT job;
    getActiveJob(job, job_name);

    TaskFilterT filter;
    filter.jobId = job.id;

    std::vector<TaskT> tasks;
    getTasks(tasks, filter);

    std::string startTime;
    std::string stopTime;
    std::string duration;

    for (std::vector<TaskT>::iterator i = tasks.begin();
                                      i != tasks.end();
                                      ++i)
    {
        
        formatDuration(duration, i->stopTime - i->startTime);
        formatTime(startTime, i->startTime);
        formatTime(stopTime, i->stopTime);

        std::cout 
            << std::left << std::setw(32) << i->name 
            << std::setw(10) << _TaskState_VALUES_TO_NAMES.find(i->state)->second
            << std::setw(18) << startTime
            << std::setw(18) << stopTime
            << std::setw(10) << duration
            << std::endl;
    }
}

} // namespace
