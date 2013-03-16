#include <arpa/inet.h>
#include <sys/socket.h>

#include <thrift/transport/TSocket.h>
#include <thrift/transport/TBufferTransports.h>
#include <thrift/protocol/TBinaryProtocol.h>

#include <boost/thread/tss.hpp>

#include <iostream>
#include <vector>

#include "client.h"

using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

PLOW_NAMESPACE_ENTER

class PlowClient::Connection
{
    public:
        Connection();
        void connect();
        void disconnect();
        RpcServiceClient proxy();        
    private:
        boost::shared_ptr<TSocket> socket;
        boost::shared_ptr<TTransport> transport;
        boost::shared_ptr<TProtocol> protocol;
        RpcServiceClient service;
};

PlowClient::Connection::Connection():
    socket(new TSocket("localhost", 11336)),
    transport(new TFramedTransport(socket)),
    protocol(new TBinaryProtocol(transport)),
    service(protocol)
{
}

void PlowClient::Connection::connect()
{
    transport->open();
}

void PlowClient::Connection::disconnect()
{
    transport->close();
}

RpcServiceClient PlowClient::Connection::proxy()
{
    return service;
}

PlowClient::PlowClient():
    m_conn(new PlowClient::Connection)
{
    m_conn->connect();
}

PlowClient::~PlowClient()
{
    m_conn->disconnect();
}

RpcServiceClient PlowClient::proxy()
{
    return m_conn->proxy();
}

PlowClient* getClient()
{
    static boost::thread_specific_ptr<PlowClient> instance;
    if(!instance.get())
    {
        instance.reset(new PlowClient);
    }
    return instance.get();
}

PLOW_NAMESPACE_EXIT




