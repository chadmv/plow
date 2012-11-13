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

extern PLOWEXPORT void getJobs(std::vector<JobT>& jobs, const JobFilterT& filter);
extern PLOWEXPORT void getTasks(std::vector<TaskT>& tasks, const TaskFilterT& filter);
extern PLOWEXPORT void getActiveJob(JobT& job, const std::string& name);

extern PLOWEXPORT void getNodes(std::vector<NodeT>& nodes, const NodeFilterT& filter);

extern PLOWEXPORT void formatTime(std::string& output, const Timestamp ts);
extern PLOWEXPORT void formatDuration(std::string& output, const int64_t startTime, const int64_t stopTime);

PLOW_NAMESPACE_EXIT

#endif
