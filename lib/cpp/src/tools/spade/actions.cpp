
#include "actions.h"

namespace Spade {

void killJob(const std::string& jobName)
{
    Plow::JobT job;
    Plow::getActiveJob(job, jobName);
    Plow::killJob(job);
}

} // namespace