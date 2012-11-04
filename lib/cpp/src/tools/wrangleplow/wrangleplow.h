
#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QTreeWidget>

namespace WranglePlow {

    class MainWindow : public QMainWindow
    {
        Q_OBJECT
        
    public:
        MainWindow();
        void updateJobs();
    
    protected:
        void closeEvent(QCloseEvent *event);

    private:
        QTreeWidget* treeWidget;
    };

}
#endif // MAINWINDOW_H