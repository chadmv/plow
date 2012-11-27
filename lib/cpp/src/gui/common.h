#ifndef PLOW_GUI_COMMON_H_
#define PLOW_GUI_COMMON_H_

#include <QApplication>
#include <QPlastiqueStyle>

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
};

}  // Gui
}  // Plow
#endif // PLOW_GUI_COMMON_H_
