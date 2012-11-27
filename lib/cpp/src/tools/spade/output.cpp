#include <iostream>
#include <iomanip>
#include <map>

#include "plow/plow.h"

#include "output.h"

using namespace Plow;

namespace Spade {

void display_job_board(const std::string& proj_name)
{   
    ProjectT proj;
    getProjectByName(proj, proj_name);

    JobBoard board;
    getJobBoard(board, proj);

    for (std::vector<FolderT>::iterator i = board.begin();
                                        i != board.end();
                                        ++i)
    {

        std::cout << i->name << std::endl;
        std::cout << "------------------------------------------------------" << std::endl;

        for(std::vector<JobT>::iterator j = i->jobs.begin();
                                        j != i->jobs.end();
                                        j++)
        {

            std::cout << j->name << std::endl;
        }

    }
}

void display_node_list()
{
    NodeFilterT filter;

    std::vector<NodeT> nodes;
    getNodes(nodes, filter);



    for (std::vector<NodeT>::iterator i = nodes.begin();
                                      i != nodes.end();
                                      ++i)
    {

        int runningCores = i->totalCores - i->idleCores;

        std::cout << std::left 
            << std::setw(28) << i->name
            << std::setw(20) << i->clusterName
            << std::setw(8) << _NodeState_VALUES_TO_NAMES.find(i->state)->second
            << std::setw(8) << _LockState_VALUES_TO_NAMES.find(i->lockState)->second
            << std::right
            << std::setfill('0') << std::setw(2) << runningCores 
            << "/" 
            << std::setfill('0') << std::setw(2) << i->totalCores
            << std::endl;
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
        
        formatDuration(duration, i->startTime, i->stopTime);
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
