#include <QApplication>
#include <QMainWindow>
#include <QVBoxLayout>
#include <QGroupBox>
#include <QPushButton>

#include "NodeTableWidget.h"
#include "NodeModel.h"

/*
 * Testing executable for comparing different approaches to
 * visualizing the data, using different tables
 *
 */


int main(int argc, char *argv[])
{
    QApplication a(argc, argv);

    QMainWindow w;
    w.resize(1200, 600);
    w.setWindowTitle("Node Manager");

    Plow::Gui::NodeModel model;
    Plow::Gui::NodeProxyModel proxy;

    proxy.setSourceModel(&model);

    QWidget* central = new QWidget;
    QVBoxLayout* layout = new QVBoxLayout(central);

    QPushButton* reload = new QPushButton("Reload");
    reload->setFixedWidth(80);
    QObject::connect(reload, SIGNAL(clicked()), &model, SLOT(populate()));

    // First table
    QGroupBox* group1 = new QGroupBox;
    group1->setTitle("Text Node Table");
    group1->setLayout(new QVBoxLayout);
    group1->layout()->setMargin(2);

    Plow::Gui::NodeTableWidget view;
    view.setModel(&proxy);
    group1->layout()->addWidget(&view);

    // Other tables


    layout->addWidget(reload, 0, Qt::AlignRight);
    layout->addWidget(group1);

    w.setCentralWidget(central);
    w.show();

    return a.exec();
}

