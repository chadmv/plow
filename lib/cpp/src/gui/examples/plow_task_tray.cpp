#include <QApplication>
#include <QMainWindow>
#include <QtGui>

#include "gui/common.h"
#include "gui/tray_widget.h"


int main(int argc, char *argv[])
{

    QApplication *a = Plow::Gui::createQApp(argc, argv);

    if (!QSystemTrayIcon::isSystemTrayAvailable()) {
        QMessageBox::critical(0, QObject::tr("Job Board Tray"),
                              QObject::tr("No system tray detected on this system."));
        return 1;
    }


    QApplication::setQuitOnLastWindowClosed(false);
    Plow::Gui::TrayWidget tray;

    return a->exec();
}
