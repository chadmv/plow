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

void getJobById(JobT& job, const Guid& id)
{
    PlowClient* client = getClient();
    client->proxy().getJob(job, id);
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

void launchJob(JobT& job, const JobSpecT& jobSpec)
{
    getClient()->proxy().launch(job, jobSpec);
}

void pauseJob(const JobT& job, const bool value)
{
    getClient()->proxy().pauseJob(job.id, value);
}

void getJobOutputs(std::vector<OutputT>& outputs, const JobT& job)
{
    getClient()->proxy().getJobOutputs(outputs, job.id);
}

void getJobBoard(JobBoard& jobBoard, const ProjectT& project) {

    getClient()->proxy().getJobBoard(jobBoard, project.id);
}


// Layers

void getLayerById(LayerT& layer, const Guid& id)
{
    getClient()->proxy().getLayerById(layer, id);
}

void getLayerByName(LayerT& layer, const JobT& job, const std::string& name)
{
    getClient()->proxy().getLayer(layer, job.id, name);
}

void getLayerOutputs(std::vector<OutputT> outputs, const LayerT& layer)
{
    getClient()->proxy().getLayerOutputs(outputs, layer.id);
}

void addLayerOutput(const LayerT& layer, const std::string& path, const Attrs& attrs)
{
    getClient()->proxy().addOutput(layer.id, path, attrs);
}

void getLayers(std::vector<LayerT>& layers, const JobT& job)
{
    getClient()->proxy().getLayers(layers, job.id);
}

// Tasks

void getTaskById(TaskT& task, const Guid& id)
{
    getClient()->proxy().getTask(task, id);
}

void getTaskLogPath(std::string& path, const TaskT& task)
{
    getClient()->proxy().getTaskLogPath(path, task.id);
}


PLOW_NAMESPACE_EXIT