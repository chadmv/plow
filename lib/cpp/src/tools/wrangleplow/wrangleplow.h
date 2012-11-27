#ifndef WRANGLE_PLOW_MAINWINDOW_H
#define WRANGLE_PLOW_MAINWINDOW_H

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

};

} // WranglePlow

#endif // WRANGLE_PLOW_MAINWINDOW_H