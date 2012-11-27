#ifndef INCLUDED_PLOW_PLOW_H
#define INCLUDED_PLOW_PLOW_H

#import <vector>

#include "plow_abi.h"

#include "common_types.h"
#include "common_constants.h"
#include "plow_types.h"
#include "plow_constants.h"

/*!rst::
C++ API
=======

*/
PLOW_NAMESPACE_ENTER

typedef std::vector<Plow::FolderT> JobBoard;

extern PLOWEXPORT void getProjectById(ProjectT& project, const Guid& id);
extern PLOWEXPORT void getProjectByName(ProjectT& project, const std::string& name);

extern PLOWEXPORT void getFolders(std::vector<FolderT>& folders, const ProjectT& project);
extern PLOWEXPORT void getFolderById(FolderT& folder, const Guid& id);
extern PLOWEXPORT void createFolder(ProjectT& project, const std::string& name);

extern PLOWEXPORT void getJobs(std::vector<JobT>& jobs, const JobFilterT& filter);
extern PLOWEXPORT void getActiveJob(JobT& job, const std::string& name);
extern PLOWEXPORT void killJob(const JobT& job);
extern PLOWEXPORT void launchJob(const JobSpecT& jobSpec);
extern PLOWEXPORT void pauseJob(const JobT& job, const bool value);
extern PLOWEXPORT void getJobById(JobT& job, const Guid& id);
extern PLOWEXPORT void getJobOutputs(std::vector<OutputT>& outputs, const JobT& job);
extern PLOWEXPORT void getJobBoard(JobBoard& jobBoard, const ProjectT& project);

extern PLOWEXPORT void getLayers(std::vector<LayerT>& layers, const JobT& job);
extern PLOWEXPORT void getLayerById(LayerT& layer, const Guid& id);
extern PLOWEXPORT void getLayerByName(LayerT& layer, const JobT& job, const std::string& name);
extern PLOWEXPORT void addLayerOutput(const LayerT& layer, const std::string& path, const Attrs& attrs);
extern PLOWEXPORT void getLayerOutputs(std::vector<OutputT> outputs, const LayerT& layer);

extern PLOWEXPORT void getTaskById(TaskT& task, const Guid& id);
extern PLOWEXPORT void getTasks(std::vector<TaskT>& tasks, const TaskFilterT& filter);

extern PLOWEXPORT void getNodes(std::vector<NodeT>& nodes, const NodeFilterT& filter);
extern PLOWEXPORT void getNode(NodeT& node, const Guid id);

extern PLOWEXPORT void formatTime(std::string& output, const Timestamp ts);
extern PLOWEXPORT void formatDuration(std::string& output, const int64_t startTime, const int64_t stopTime);

PLOW_NAMESPACE_EXIT

#endif
