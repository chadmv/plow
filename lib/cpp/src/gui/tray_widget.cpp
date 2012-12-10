
#include <QApplication>
#include <QVBoxLayout>
#include <QMenu>

#include "tray_widget.h"

namespace Plow {
namespace Gui {

TrayWidget::TrayWidget()
{
    jobBoard = 0;
    createActions();
    createTrayIcon();
    trayIcon->show();
    mainLayout = new QVBoxLayout();
    this->setLayout(mainLayout);
    resize(1200, 300);
}


void TrayWidget::createTrayIcon()
{
    trayIconMenu = new QMenu(this);
    trayIconMenu->addAction(wranglePlowAction);
    trayIconMenu->addAction(quitAction);


    trayIcon = new QSystemTrayIcon(this);
    trayIcon->setContextMenu(trayIconMenu);

    QIcon icon = QIcon(":images/plow_icon.svg");
    trayIcon->setIcon(icon);
    trayIcon->setToolTip("Plow System Tray");

    connect(trayIcon, SIGNAL(activated(QSystemTrayIcon::ActivationReason)),
            this, SLOT(iconActivated(QSystemTrayIcon::ActivationReason)));

}

void TrayWidget::showEvent(QShowEvent * event)
{
    if (!jobBoard)
    {
        jobBoard = new Plow::Gui::JobBoardWidget(this);
        mainLayout->addWidget(jobBoard);
    }
}

void TrayWidget::hideEvent(QHideEvent * event)
{
    delete jobBoard;
    jobBoard = 0;
}

void TrayWidget::onWranglePlow()
{
    qDebug("not implemented");
}

void TrayWidget::createActions()
{
    wranglePlowAction = new QAction(tr("&WranglePlow"), this);
    connect(wranglePlowAction, SIGNAL(triggered()), this, SLOT(onWranglePlow()));

    quitAction = new QAction(tr("&Quit"), this);
    connect(quitAction, SIGNAL(triggered()), qApp, SLOT(quit()));
}

void TrayWidget::showJobBoard()
{

    if (this->isVisible())
    {
        this->hide();
    }
    else
    {
        this->show();
        this->raise();
        this->activateWindow();
    }
}

void TrayWidget::iconActivated(QSystemTrayIcon::ActivationReason reason)
{
    switch (reason) {
    case QSystemTrayIcon::Trigger:
    case QSystemTrayIcon::DoubleClick:
        TrayWidget::showJobBoard();
        break;
    case QSystemTrayIcon::MiddleClick:
        break;
    default:
        ;
    }
}

}  // Gui
}  // Plow
