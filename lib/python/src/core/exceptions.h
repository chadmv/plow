#ifndef INCLUDED_PLOW_EXCEPTIONS_H
#define INCLUDED_PLOW_EXCEPTIONS_H

#include "rpc/plow_types.h"

namespace
{
  static char py_plow_exception[] = "plow.PlowException";
  static PyObject* PyExc_PlowException = PyErr_NewException(py_plow_exception, 
                                                            PyExc_StandardError, 
                                                            NULL);
}

#define __Pyx_CppExn2PyErr plow_exception_handler

static void __Pyx_CppExn2PyErr() {
  // Catch a handful of different errors here and turn them into the
  // equivalent Python errors.
  try {
    if (PyErr_Occurred())
      ; // let the latest Python exn pass through and ignore the current one
    else
      throw;
  } 
  catch (const Plow::PlowException& exn) {
    PyObject *args = Py_BuildValue("(is)", exn.what, exn.why.c_str());
    // PyErr_SetObject(PyExc_PlowException, args);
    PyErr_SetObject(PyExc_StandardError, args);
    Py_DECREF(args);
  } 
  catch (const std::bad_alloc& exn) {
    PyErr_SetString(PyExc_MemoryError, exn.what());
  } 
  catch (const std::bad_cast& exn) {
    PyErr_SetString(PyExc_TypeError, exn.what());
  } 
  catch (const std::domain_error& exn) {
    PyErr_SetString(PyExc_ValueError, exn.what());
  } 
  catch (const std::invalid_argument& exn) {
    PyErr_SetString(PyExc_ValueError, exn.what());
  } 
  catch (const std::ios_base::failure& exn) {
    // Unfortunately, in standard C++ we have no way of distinguishing EOF
    // from other errors here; be careful with the exception mask
    PyErr_SetString(PyExc_IOError, exn.what());
  } 
  catch (const std::out_of_range& exn) {
    // Change out_of_range to IndexError
    PyErr_SetString(PyExc_IndexError, exn.what());
  } 
  catch (const std::overflow_error& exn) {
    PyErr_SetString(PyExc_OverflowError, exn.what());
  } 
  catch (const std::range_error& exn) {
    PyErr_SetString(PyExc_ArithmeticError, exn.what());
  } 
  catch (const std::underflow_error& exn) {
    PyErr_SetString(PyExc_ArithmeticError, exn.what());
  } 
  catch (const std::exception& exn) {
    // PyErr_SetString(PyExc_RuntimeError, "foo");
    PyErr_SetString(PyExc_RuntimeError, exn.what());
  }
  catch (...)
  {
    PyErr_SetString(PyExc_RuntimeError, "Unknown exception");
  }
}

#endif