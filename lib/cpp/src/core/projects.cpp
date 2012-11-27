#include <vector>
#include <string>
#include <sstream>
#include <cstdlib>

#include "plow/plow.h"
#include "client.h"

PLOW_NAMESPACE_ENTER

void getProjectById(ProjectT& project, const Guid& id)
{
    getClient()->proxy().getProject(project, id);
}

void getProjectByName(ProjectT& project, const std::string& name)
{
    getClient()->proxy().getProjectByName(project, name);
}

PLOW_NAMESPACE_EXIT