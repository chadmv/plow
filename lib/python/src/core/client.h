#ifndef INCLUDED_PLOW_CLIENT_H
#define INCLUDED_PLOW_CLIENT_H

#include "plow_abi.h"
#include "rpc/RpcService.h"

PLOW_NAMESPACE_ENTER

class PLOWEXPORT PlowClient
{
    public:
        PlowClient();
        PlowClient(const std::string& host, const int32_t port);
        virtual ~PlowClient();
        RpcServiceClient proxy();
        void reconnect();
    private:
        class Connection;
        friend class Connection;
        Connection * m_conn;
};

extern PlowClient* getClient(const bool reset = 0);
extern PlowClient* getClient(const std::string& host, const int32_t port, const bool reset = 0);

PlowClient* _getClient(const std::string& host, const int32_t port, const bool reset = 0);


PLOW_NAMESPACE_EXIT

#endif