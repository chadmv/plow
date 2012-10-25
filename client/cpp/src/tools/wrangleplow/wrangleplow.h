#ifndef WRANGLEPLOW_H
#define WRANGLEPLOW_H

#include <QMainWindow>
#include <QWidget>

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
    
private:
    Ui::MainWindow *ui;
};

}
}
#endif // WRANGLEPLOW_H