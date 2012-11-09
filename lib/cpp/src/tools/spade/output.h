
#ifndef SPADE_OUTPUT_H
#define SPADE_OUTPUT_H

#include <string>

namespace Spade {

void display_job_list();
void display_task_list(const std::string& job_name);

}
#endif // SPADE_OUTPUT_H