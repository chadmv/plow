#include <QApplication>
#include <QMainWindow>

#include "gui/common.h"
#include "gui/task_pie.h"


//
// Main
//
int main(int argc, char *argv[])
{
    QApplication *a = Plow::Gui::createQApp(argc, argv);

    QMainWindow w;
    w.resize(600, 800);
    w.setWindowTitle("TaskPie");

    
    Plow::Gui::TaskPieWidget pie;
    w.setCentralWidget(&pie);
    w.show();

    // User should hit ctrl/cmd + o to open a new log

    return a->exec();
}

