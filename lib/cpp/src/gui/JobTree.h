#ifndef JOBTREE_H
#define JOBTREE_H

#include <QTreeWidget>
#include <QVBoxLayout>

namespace Plow { namespace Gui {

    class JobTree : public QWidget
    {
        Q_OBJECT
        
    public:
        JobTree(QWidget* parent=0);
        void updateJobs();

    private:
        QVBoxLayout* layout;
        QTreeWidget* treeWidget;
    };


} // Gui
} // Plow

#endif // JOBTREE_H