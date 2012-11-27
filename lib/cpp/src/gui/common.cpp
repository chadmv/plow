#include "common.h"

namespace Plow {
namespace Gui {

// createQApp
QApplication* createQApp(int &argc, char **argv, int flags)
{
    PlowStyle *plowStyle = new PlowStyle;
    QApplication::setStyle(plowStyle);

    QApplication *app  = new QApplication(argc, argv, flags);
    app->setStyleSheet(
                "QScrollBar {"
                "  border: 2px solid rgb(30,29,28);"
                "  border-radius: 6px;"
                "  background: rgb(18,18,16);"
                "  margin: 0 0 0 0;"
                "}"
                "QScrollBar:vertical {"
//                "  background: qlineargradient(x1:0, y1:0, x2:1, y2:0, "
//                "              stop:0 rgb(255,255,18), stop:1 rgb(30,29,28));"
                "  width: 15px;"
                "}"
                "QScrollBar:horizontal {"
//                "  background: qlineargradient(x1:0, y1:1, x2:0, y2:0,"
//                "              stop:0 rgb(19,18,18), stop:1 rgb(30,29,28));"
                "  height: 15px;"
                "}"
                "QScrollBar::handle {"
                "  border-radius: 4px;"
                "}"
                "QScrollBar::handle:vertical {"
                "  background: qlineargradient(x1:0, y1:0, x2:1, y2:0,"
                "              stop:0 rgb(67,66,63), stop:1 rgb(39,38,35));"
                "  min-height: 20px;"
                "}"
                "QScrollBar::handle:horizontal {"
                "  background: qlineargradient(x1:0, y1:0, x2:0, y2:1,"
                "              stop:0 rgb(67,66,63), stop:1 rgb(39,38,35));"
                "  min-width: 20px;"
                "}"
                "QScrollBar::add-line:vertical, QScrollBar::add-line:horizontal {"
                "  width: 1px;"
                "  height: 1px;"
                "}"
                "QScrollBar::sub-line:vertical, QScrollBar::sub-line:horizontal {"
                "  width: 1px;"
                "  height: 1px;"
                "}"
            );

    return app;
}

QList<QColor> PlowStyle::TaskColors = QList<QColor>()
    << QColor(60, 59, 55) // INIT - should never see
    << QColor(22, 47, 167) // WAITING
    << QColor(167, 159, 22) // RUNNING
    << QColor(167, 20, 14) // DEAD
    << QColor(116, 5, 0) // EATEN
    << QColor(166, 12, 143) // DEPEND
    << QColor(111, 167, 14); // SUCCEEEDED

// PlowStyle
PlowStyle::PlowStyle() :
    QPlastiqueStyle()
{}

void PlowStyle::polish(QPalette &palette) {

    QColor grayish(73, 72, 68);

    palette = QPalette(grayish);

    palette.setBrush(QPalette::Window, QColor(60, 59, 55));
    palette.setBrush(QPalette::WindowText, QColor(195, 194, 190));
    palette.setBrush(QPalette::ButtonText, QColor(211, 207, 196));
    palette.setBrush(QPalette::Text, QColor(223, 219, 207));
    palette.setBrush(QPalette::BrightText, QColor(224, 218, 220));

    QLinearGradient baseGrad(0, 0, 1, 1);
    baseGrad.setColorAt(0, QColor(29, 29, 27));
    baseGrad.setColorAt(1, QColor(38, 38, 36));
    palette.setBrush(QPalette::Base, baseGrad);

    QLinearGradient buttonGrad(0, 0, 1, 1);
    buttonGrad.setColorAt(0, QColor(75, 74, 70));
    buttonGrad.setColorAt(1, QColor(43, 42, 38));
    palette.setBrush(QPalette::Button, buttonGrad);

    palette.setBrush(QPalette::Highlight, QColor(105, 55, 88));
    palette.setBrush(QPalette::HighlightedText, Qt::white);

    QBrush brush = palette.background();
    brush.setColor(brush.color().dark());

    palette.setBrush(QPalette::Disabled, QPalette::WindowText, brush);
    palette.setBrush(QPalette::Disabled, QPalette::Text, brush);
    palette.setBrush(QPalette::Disabled, QPalette::ButtonText, brush);
    palette.setBrush(QPalette::Disabled, QPalette::Base, brush);
    palette.setBrush(QPalette::Disabled, QPalette::Button, brush);
    palette.setBrush(QPalette::Disabled, QPalette::Mid, brush);
}

}
}
