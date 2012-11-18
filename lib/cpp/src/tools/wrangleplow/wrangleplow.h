
#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QTreeWidget>

#include "gui/JobTree.h"

namespace WranglePlow {

    class MainWindow : public QMainWindow
    {
        Q_OBJECT
        
    public:
        MainWindow();

    protected:
        void closeEvent(QCloseEvent *event);

    private:
        Plow::Gui::JobTree* jobTree;
    };

}
#endif // MAINWINDOW_H