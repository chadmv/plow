#ifndef INCLUDED_PLOW_CONNECTION_H
#define INCLUDED_PLOW_CONNECTION_H

#include "plow/plow.h"

#include "rpc/RpcServiceApi.h"
#include "rpc/plow_types.h"

#include <arpa/inet.h>
#include <sys/socket.h>
#include <transport/TSocket.h>
#include <transport/TBufferTransports.h>
#include <protocol/TBinaryProtocol.h>

using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

PLOW_NAMESPACE_ENTER
{
  class Connection
  {
  public:
    Connection(int port);
    ~Connection();
    void connect();
    void disconnect();
    rpc::RpcServiceApiClient getService();        
  private:
    boost::shared_ptr<TSocket> socket;
    boost::shared_ptr<TTransport> transport;
    boost::shared_ptr<TProtocol> protocol;
    rpc::RpcServiceApiClient service;
  };
}
PLOW_NAMESPACE_EXIT

#endif