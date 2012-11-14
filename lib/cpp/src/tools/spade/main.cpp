#include <string>

#include <boost/program_options.hpp>

#include "output.h"
#include "actions.h"

#include "plow/plow.h"

namespace opt = boost::program_options;

namespace 
{ 
    const size_t COMMAND_LINE_ERROR = 1; 
    const size_t COMMAND_LINE_SUCCESS = 0;
}

int main(int argc, char *argv[])
{
    // Standard options
    opt::options_description action_options("Action Options");
    action_options.add_options()
        ("help", "display help")
        ("lj", "display list of active jobs")
        ("lt", "display list of tasks")
        ("ln", "display list of nodes")
        ("kill", "kill specified job, layer, or tasks")
        ;

    // Query Options
    opt::options_description query_options("Query Options");
    query_options.add_options()
        ("job,j", opt::value<std::string>(), "JOB NAME")
        ("layer,l", opt::value<std::string>(), "LAYER NAME")
        ("task,t", opt::value<std::string>(), "TASK NAME")
        ;

    // Standard options
    opt::options_description all_options;
    all_options.add(action_options).add(query_options);

    try 
    {
        opt::variables_map vm;
        opt::store(
            opt::command_line_parser(argc, argv)
            .options(all_options)
            .style (opt::command_line_style::default_style |
                    opt::command_line_style::allow_long_disguise)
            .run(), vm);
        opt::notify(vm);    

        if (vm.count("help"))
        {
            std::cout << all_options << "\n";
            return 1;
        }

        if (vm.count("lj"))
        {
            Spade::display_job_list();
        }
        else if (vm.count("lt"))
        {
            if (vm.count("job"))
            {
                Spade::display_task_list(vm["job"].as<std::string>());
            }
            else
            {
                std::cerr << "ERROR: You must specify at least a job when listing tasks." << std::endl;
            }
        }
        else if (vm.count("ln"))
        {
            Spade::display_node_list();
        }
        else if (vm.count("kill"))
        {

            if (vm.count("job") && !vm.count("layer") && !vm.count("task"))
            {
                Spade::killJob(vm["job"].as<std::string>());
            }
        }

    }
    catch(const opt::error& e) 
    { 
        std::cerr << "ERROR: " << e.what() << std::endl << std::endl; 
        std::cerr << all_options << std::endl; 
        return COMMAND_LINE_ERROR; 
    }
    catch(const std::exception& ex)
    {
        std::cerr << "ERROR: " << ex.what() << std::endl << std::endl; 
        std::cerr << all_options << std::endl; 
        return COMMAND_LINE_ERROR;    
    }

    return COMMAND_LINE_SUCCESS;
}



