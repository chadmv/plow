#ifndef PLOW_JOB_BOARD_H
#define PLOW_JOB_BOARD_H

#include <map>

#include <QTreeWidget>
#include <QDockWidget>
#include <QTreeWidgetItem>
// #include <QAction>

#include "plow/plow.h"

Q_DECLARE_METATYPE( Plow::JobT* )

namespace Plow { 
namespace Gui {

typedef std::map<std::string, QTreeWidgetItem*> ItemIndex;

class JobBoardWidget : public QWidget
{
    Q_OBJECT
    
public:
    JobBoardWidget(QWidget* parent=0);
    ~JobBoardWidget();

public slots:
    void load();
    void refresh();
    void itemSelected(QTreeWidgetItem *item, int column);

private slots:    
    void onJobKill();

signals:
    void jobSelected(QString id);

private:

    void addJobItem(QTreeWidgetItem *parent, JobT* job);
    void defineActions();
    QTreeWidgetItem* addFolderItem(FolderT* folder);

    ItemIndex* itemIndex;
    QTreeWidget* treeWidget;
    JobBoard jobBoard;
    qint64 updateCounter;
};

} // Gui
} // Plow

#endif // PLOW_JOB_BOARD_H