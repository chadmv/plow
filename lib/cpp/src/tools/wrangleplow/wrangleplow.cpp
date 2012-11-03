#include "wrangleplow.h"
#include "ui_wrangleplow.h"

#include "plow.h"

#include <vector>

namespace Plow {
namespace WranglePlow {

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::updateJobs()
{
    PlowClient* client = getClient();

    std::vector<JobT> jobs;
    JobFilterT filter;

    std::vector<JobState::type> states;
    states.push_back(JobState::RUNNING);
    filter.__set_states(states);

    client->getJobs(jobs, filter);

    QTreeWidget *tree = ui->treeWidget;
    for (std::vector<JobT>::iterator i = jobs.begin();
                                     i != jobs.end();
                                     ++i)
    {
        QTreeWidgetItem *item = new QTreeWidgetItem((QTreeWidget*)0,
            QStringList(QString(i->name.c_str())));
        tree->insertTopLevelItem(0, item);
    }
}

}
}