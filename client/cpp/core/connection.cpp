#include "connection.h"

#include <iostream>

using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

PLOW_NAMESPACE_ENTER
{
  Connection::Connection(int port):
    socket(new TSocket("localhost", port)),
    transport(new TFramedTransport(socket)),
    protocol(new TBinaryProtocol(transport)),
    service(protocol)
  {
    
  }
  Connection::~Connection()
  {
  }
  
  void Connection::connect()
  {
    transport->open();
  }
  
  void Connection::disconnect() {
    transport->close();
  }
  
  rpc::RpcServiceApiClient Connection::getService() {
    return service;
  }
}
PLOW_NAMESPACE_EXIT




