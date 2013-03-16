#ifndef INCLUDED_PLOW_PLOWABI_H
#define INCLUDED_PLOW_PLOWABI_H

#define PLOW_NAMESPACE Plow

#define PLOW_VERSION "1.0.0"
#define PLOW_VERSION_NS v1

#define PLOW_NAMESPACE_ENTER namespace PLOW_NAMESPACE {
#define PLOW_NAMESPACE_EXIT }
#define PLOW_NAMESPACE_USING using namespace PLOW_NAMESPACE;

#define PLOWEXPORT __attribute__ ((visibility("default")))
#define PLOWHIDDEN __attribute__ ((visibility("hidden")))

#include <boost/shared_ptr.hpp>
#define PLOW_SHARED_PTR boost::shared_ptr
#define PLOW_DYNAMIC_POINTER_CAST boost::dynamic_pointer_cast
  
#endif
