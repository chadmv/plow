
#include "plow.h"
#include "rpc/RpcServiceApi.h"

#include <arpa/inet.h>
#include <sys/socket.h>
#include <transport/TSocket.h>
#include <transport/TBufferTransports.h>
#include <protocol/TBinaryProtocol.h>

#include <boost/thread/tss.hpp>

#include <iostream>
#include <vector>

using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

PLOW_NAMESPACE_ENTER

class PlowClient::Connection
{
    public:
        Connection();
        ~Connection();
        void connect();
        void disconnect();
        RpcServiceApiClient getService();        
    private:
        boost::shared_ptr<TSocket> socket;
        boost::shared_ptr<TTransport> transport;
        boost::shared_ptr<TProtocol> protocol;
        RpcServiceApiClient service;
};

PlowClient::Connection::Connection():
    socket(new TSocket("localhost", 11336)),
    transport(new TFramedTransport(socket)),
    protocol(new TBinaryProtocol(transport)),
    service(protocol)
{
}
    
PlowClient::Connection::~Connection()
{
}

void PlowClient::Connection::connect()
{
    transport->open();
}

void PlowClient::Connection::disconnect() {
    transport->close();
}

RpcServiceApiClient PlowClient::Connection::getService() {
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

void PlowClient::getJobs(std::vector<JobT>& jobs, const JobFilter& filter) const
{
    m_conn->getService().getJobs(jobs, filter);
}


PlowClient* getConnection()
{
    static boost::thread_specific_ptr<PlowClient> instance;
    if(!instance.get())
    {
        instance.reset(new PlowClient);
    }
    return instance.get();
}

PLOW_NAMESPACE_EXIT




