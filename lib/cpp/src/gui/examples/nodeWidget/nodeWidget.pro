#-------------------------------------------------
#
# Project created by QtCreator 2012-11-09T11:11:26
#
#-------------------------------------------------

QT       += core gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = nodeWidget
TEMPLATE = app


SOURCES += main.cpp \
    ../../NodeTableWidget.cpp \
    ../../NodeModel.cpp

HEADERS  += \
    ../../NodeTableWidget.h \
    ../../NodeModel.h

INCLUDEPATH += ../../
