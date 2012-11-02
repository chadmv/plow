#ifndef INCLUDED_PLOW_PLOW_H
#define INCLUDED_PLOW_PLOW_H

#import <vector>

#include "plowABI.h"
#include "plowTypes.h"

#include "common_types.h"
#include "common_constants.h"
#include "plow_types.h"
#include "plow_constants.h"


/*!rst::
C++ API
=======

*/

PLOW_NAMESPACE_ENTER

class PLOWEXPORT PlowClient
{
    public:
        PlowClient();
        ~PlowClient();

        void getJobs(std::vector<JobT>& jobs, const JobFilter& filter) const;

    private:
        class Connection;
        friend class Connection;
        Connection * m_conn;
};

extern PLOWEXPORT PlowClient* getConnection();

PLOW_NAMESPACE_EXIT

#endif
