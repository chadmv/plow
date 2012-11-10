#include "plow.h"

#include <ctime>
#include <string>
#include <sstream>
#include <iomanip>

PLOW_NAMESPACE_ENTER

const char* NO_TIME = "__-__ __:__:__";

void formatTime(std::string& output, const Timestamp ts) 
{   
    if (ts == 0)
    {
        output.assign(NO_TIME);
        return;
    }

    const time_t epoch_time = (time_t)ts;
    struct tm * timeinfo;
    timeinfo = std::localtime(&epoch_time);

    char buffer [16];
    strftime (buffer, 15, "%m-%d %H:%M:%S", timeinfo);
    output.assign(buffer);
}

void formatDuration(std::string& output, const int duration)
{
    using namespace std;

    int sec = duration % 60;
    int min = duration / 60;
    int hour = min / 60;

    if (sec > 3600)
    {
        min = min % min;
    }
    stringstream ss;
    ss 
        << setfill('0') 
        << setw(2)
        << hour
        << ":" 
        << setw(2) 
        << min 
        << ":" 
        << setw(2)
        << sec
        << setfill(' ');
    output.assign(ss.str());
}

PLOW_NAMESPACE_EXIT