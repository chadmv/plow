#include <vector>
#include <string>

#include "plow/plow.h"
#include "client.h"

PLOW_NAMESPACE_ENTER

void getNodes(std::vector<NodeT>& nodes, const NodeFilterT& filter)
{
    PlowClient* client = getClient();
    client->proxy().getNodes(nodes, filter);
}

void getNode(NodeT& node, const Guid id)
{
    getClient()->proxy().getNode(node, id);
}

PLOW_NAMESPACE_EXIT