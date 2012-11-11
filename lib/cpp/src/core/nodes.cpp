
#include "plow.h"
#include "client.h"

#include <vector>
#include <string>

PLOW_NAMESPACE_ENTER

void getNodes(std::vector<NodeT>& nodes, const NodeFilterT& filter)
{
    PlowClient* client = getClient();
    client->proxy().getNodes(nodes, filter);
}

PLOW_NAMESPACE_EXIT