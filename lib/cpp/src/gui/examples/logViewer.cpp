#include <QApplication>
#include <QMainWindow>

#include "gui/common.h"
#include "gui/logviewer.h"


//
// Main
//
int main(int argc, char *argv[])
{
    QApplication *a = Plow::Gui::createQApp(argc, argv);

    QMainWindow w;
    w.resize(600, 800);
    w.setWindowTitle("Log Viewer");

    Plow::Gui::LogViewer loggy;
    loggy.setInterval(3000); // check log every 3 seconds
    w.setCentralWidget(&loggy);
    w.show();

    // User should hit ctrl/cmd + o to open a new log

    return a->exec();
}

