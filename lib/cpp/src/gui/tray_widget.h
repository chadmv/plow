#ifndef TRAYWIDGET_H
#define TRAYWIDGET_H


#include <QSystemTrayIcon>
#include <QDialog>
#include <QVBoxLayout>

#include "gui/job_board.h"

namespace Plow {
namespace Gui {


class TrayWidget : public QDialog
{
    Q_OBJECT

private slots:
    void iconActivated(QSystemTrayIcon::ActivationReason reason);
    void onWranglePlow();

protected:
    void hideEvent(QHideEvent * event);
    void showEvent(QShowEvent * event);

public:
    TrayWidget();
    QSystemTrayIcon *trayIcon;
    QMenu *trayIconMenu;
    Plow::Gui::JobBoardWidget* jobBoard;

private:
    void createTrayIcon();
    void createActions();
    void showJobBoard();
    QAction *quitAction;
    QAction *wranglePlowAction;
    QVBoxLayout *mainLayout;
};


}  // Gui
}  // Plow

#endif // TRAYWIDGET_H
