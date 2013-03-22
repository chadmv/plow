

                            
#######################
# Nodes
#

cdef class Node:
    def __init__(self):
        raise NotImplementedError

# def get_node(string name):
#     cdef: 
#         NodeT nodeT
#         Node node 

#     getClient().proxy().getNode(nodeT, name)
#     node = initNode(nodeT)
#     return node

# def get_nodes(NodeFilter filter):
#     cdef:
#         NodeFilterT filterT 
#         NodeT nodeT
#         vector[NodeT] nodes 
#         list ret = []

#     getClient().proxy().getNodes(nodes, filterT)
#     ret = [initNode(nodeT) for nodeT in nodes]
#     return ret

