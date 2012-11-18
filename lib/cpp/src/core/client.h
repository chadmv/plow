#ifndef INCLUDED_PLOW_CLIENT_H
#define INCLUDED_PLOW_CLIENT_H

#include "plow/plow_abi.h"
#include "rpc/RpcService.h"

PLOW_NAMESPACE_ENTER

class PLOWEXPORT PlowClient
{
    public:
        PlowClient();
        virtual ~PlowClient();
        RpcServiceClient proxy();
    private:
        class Connection;
        friend class Connection;
        Connection * m_conn;
};

extern PlowClient* getClient();

PLOW_NAMESPACE_EXIT

#endif