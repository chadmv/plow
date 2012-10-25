#include "wrangleplow.h"
#include "ui_wrangleplow.h"

namespace Plow {
namespace WranglePlow {

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);
}

MainWindow::~MainWindow()
{
    delete ui;
}

}
}