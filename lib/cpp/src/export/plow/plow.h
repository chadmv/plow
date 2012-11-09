#ifndef INCLUDED_PLOW_PLOW_H
#define INCLUDED_PLOW_PLOW_H

#import <vector>

#include "plow_abi.h"

#include "common_types.h"
#include "common_constants.h"
#include "plow_types.h"
#include "plow_constants.h"
#include "RpcService.h"


/*!rst::
C++ API
=======

*/

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

extern PLOWEXPORT PlowClient* getClient();
extern PLOWEXPORT void getJobs(std::vector<JobT>& jobs, const JobFilterT& filter);

PLOW_NAMESPACE_EXIT

#endif
