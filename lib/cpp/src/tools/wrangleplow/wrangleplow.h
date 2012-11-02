#ifndef WRANGLEPLOW_H
#define WRANGLEPLOW_H

#include <QMainWindow>
#include <QString>
#include <QStringList>
#include <QTreeWidget>
#include <QTreeWidgetItem>

namespace Ui {
    class MainWindow;
}


namespace Plow {
namespace WranglePlow {

class MainWindow : public QMainWindow
{
    Q_OBJECT
    
public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

    void updateJobs();
        
private:
    Ui::MainWindow *ui;
};

}
}
#endif // WRANGLEPLOW_H