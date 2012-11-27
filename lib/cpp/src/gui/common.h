#ifndef PLOW_GUI_COMMON_H_
#define PLOW_GUI_COMMON_H_

#include <QApplication>
#include <QPlastiqueStyle>
#include <QColor>
#include <QList>

class QPalette;

namespace Plow {
namespace Gui {

// createQApp
// Convenience function for creation a QApplication
// and setting style options.
QApplication* createQApp(int &argc, char **argv,
                         int flags = QApplication::ApplicationFlags);

// PlowStyle
// Custom QStyle class for Plow apps
class PlowStyle : public QPlastiqueStyle
{
    Q_OBJECT
public:
    PlowStyle();
    void polish(QPalette &palette);
    static QList<QColor> TaskColors;


};

}  // Gui
}  // Plow
#endif // PLOW_GUI_COMMON_H_
