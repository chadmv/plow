#ifndef PLOW_GUI_EXAMPLES_FIXTURE_P_H_
#define PLOW_GUI_EXAMPLES_FIXTURE_P_H_

#include "NodeModel.h"

class QObject;

namespace Plow {
namespace Gui {

class DataFixture : public QObject {
    Q_OBJECT

 public:
    DataFixture(NodeModel* aModel, QObject *parent=0);

    NodeList getHosts(const int &amount) const;

    int host_count;

 public slots:
    void updateData();

 private:
    NodeModel* model;
};

}  // Gui
}  // Plow

#endif // PLOW_GUI_EXAMPLES_FIXTURE_P_H_
