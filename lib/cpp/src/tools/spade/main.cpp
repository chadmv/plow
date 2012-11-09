#include "output.h"

#include <string>
#include <boost/program_options.hpp>

namespace opt = boost::program_options;

int main(int argc, char *argv[])
{
    // Standard options
    opt::options_description options("Standard Options");
    options.add_options()
        ("help", "produce help message")
        ("job", opt::value<std::string>(), "job name")
        ("jobs", "display list of active jobs")
        ;

    opt::variables_map vm;
    opt::store(
        opt::command_line_parser(argc, argv)
        .options(options)
        .run(), vm);
    opt::notify(vm);    

    if (vm.count("help")) {
        std::cout << options << "\n";
        return 1;
    }

    if (vm.count("job"))
    {
        std::cout << "Job " 
            << vm["job"].as<std::string>() << "\n";
    }

    if (vm.count("jobs")) {
        Spade::display_job_list();
        return 0;
    }

    return 0;
}



