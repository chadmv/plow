#ifndef INCLUDED_PLOW_PLOW_H
#define INCLUDED_PLOW_PLOW_H

#import <vector>

#include "plowABI.h"
#include "plowTypes.h"

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

        void getJobs() const;

    private:
        class Connection;
        friend class Connection;
        Connection * m_conn;
};

PLOW_NAMESPACE_EXIT

#endif