
#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include "JobTree.h"

#include <QMainWindow>
#include <QTreeWidget>

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