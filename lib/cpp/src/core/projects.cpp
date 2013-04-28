#include <vector>
#include <string>
#include <sstream>
#include <cstdlib>

#include "plow/plow.h"
#include "client.h"

PLOW_NAMESPACE_ENTER


uint64_t getPlowTime()
{
    return getClient()->proxy().getPlowTime();
}

void getProjectById(ProjectT& project, const Guid& id)
{
    getClient()->proxy().getProject(project, id);
}

void createFolder(FolderT& folder, const ProjectT& project, const std::string& name)
{
    getClient()->proxy().createFolder(folder, project.id, name);
}

void getFolders(std::vector<FolderT>& folders, const ProjectT& project)
{
    getClient()->proxy().getFolders(folders, project.id);
}

void getFolder(FolderT& folder, const Guid& id)
{
    getClient()->proxy().getFolder(folder, id);
}

PLOW_NAMESPACE_EXIT
