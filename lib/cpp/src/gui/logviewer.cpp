#include <QPlainTextEdit>
#include <QFileSystemWatcher>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QStringList>
#include <QLineEdit>
#include <QComboBox>
#include <QCheckBox>
#include <QTextBlock>
#include <QLabel>
#include <QScrollBar>
#include <QPushButton>
#include <QAction>
#include <QFileDialog>
#include <QDebug>

#include "logviewer.h"

namespace Plow {
namespace Gui {

LogViewer::LogViewer(QWidget *parent) :
    QWidget(parent)
{
    QVBoxLayout* mainLayout = new QVBoxLayout(this);
    mainLayout->setObjectName("mainLayout");

    QAction *openAction = new QAction("Open Log File", this);
    openAction->setShortcut(QKeySequence::Open);
    addAction(openAction);

    searchLine = new QLineEdit(this);
    logTailCheckbox = new QCheckBox("Tail log:", this);
    taskSelector = new QComboBox(this);

    QPushButton *findPrevBtn = new QPushButton("Prev", this);
    QPushButton *findNextBtn = new QPushButton("Next", this);
    findPrevBtn->setFixedWidth(35);
    findNextBtn->setFixedWidth(35);

    QHBoxLayout* controlLayout = new QHBoxLayout;
    controlLayout->addWidget(new QLabel(tr("Find:")));
    controlLayout->addWidget(searchLine);
    controlLayout->addWidget(findPrevBtn);
    controlLayout->addWidget(findNextBtn);
    controlLayout->addStretch();
    controlLayout->addWidget(logTailCheckbox);
    controlLayout->addWidget(new QLabel(tr("Task:")));
    controlLayout->addWidget(taskSelector);

    mainLayout->addLayout(controlLayout);

    view = new QPlainTextEdit(this);
    QFont font = view->font();
    font.setPointSize(font.pointSize()-2);
    font.setWeight(font.Light);
    view->setFont(font);
    view->setLineWrapMode(view->WidgetWidth);
    view->setReadOnly(true);
    // TODO: Handle logs that overflow this
    // pretty sizeable amount (1 mil paragraphs)
    view->setMaximumBlockCount(1000000);
    mainLayout->addWidget(view);

    setLayout(mainLayout);

    logWatcher = new QFileSystemWatcher(this);

    // Connections
    connect(logWatcher, SIGNAL(fileChanged(QString)),
            this, SLOT(logUpdated()));
    connect(logTailCheckbox, SIGNAL(stateChanged(int)),
            this, SLOT(logTailToggled(int)));
    connect(searchLine, SIGNAL(textChanged(QString)),
            this, SLOT(findText(QString)));
    connect(findPrevBtn, SIGNAL(clicked()), this, SLOT(findPrev()));
    connect(findNextBtn, SIGNAL(clicked()), this, SLOT(findNext()));
    connect(openAction, SIGNAL(triggered()), this, SLOT(openLogFile()));
}


void LogViewer::setCurrentTask(const TaskT &task) {
    currentTask = task;
}

void LogViewer::setLogPath(const QString &path) {
    stopLogTail();

    openLog.close();
    openLog.setFileName(path);
    if (!openLog.open(QIODevice::ReadOnly|QIODevice::Text)) {
        qDebug() << "Failed to open file" << path;
        return;
    }

    view->clear();

    logStream.setDevice(&openLog);
    view->setPlainText(QString(logStream.readAll()));

    if (logTailCheckbox->isChecked())
        startLogTail();
}

void LogViewer::openLogFile() {
    QString logpath = QFileDialog::getOpenFileName(this,
                                                   "Open a log file",
                                                   QString(),
                                                   "Logs (*.log *.txt);;All Files (*)");
    if (!logpath.isEmpty())
        setLogPath(logpath);
}

void LogViewer::findText(const QString &text, const QTextCursor &cursor,
                         QTextDocument::FindFlags opts) {
    QTextCursor newCursor = view->document()->find(text, cursor, opts);
    if (newCursor.isNull()) {
        qDebug() << "findText: nothing found";
    } else {
        view->setTextCursor(newCursor);
    }
    view->centerCursor();
}

void LogViewer::findPrev() {
    QTextCursor cursor = view->textCursor();
    findText(searchLine->text(), cursor, QTextDocument::FindBackward);
}

void LogViewer::findNext() {
    QTextCursor cursor = view->textCursor();
    findText(searchLine->text(), cursor);
}

void LogViewer::logUpdated() {
    QString line;
    QTextCursor cursor = view->textCursor();
    cursor.movePosition(cursor.End);

    do {
        line = logStream.read(1024);
        if (!line.isEmpty()) {
            cursor.insertText(line);
        }
    } while (!line.isNull());

    // Move to the bottom if we are tailing and they
    // don't have a current search filter active
    if (searchLine->text().trimmed().isEmpty()) {
        int maxVal = view->verticalScrollBar()->maximum();
        view->verticalScrollBar()->setValue(maxVal);
    }
}

void LogViewer::stopLogTail() {
    QStringList paths(logWatcher->files());
    if (!paths.isEmpty())
        logWatcher->removePaths(paths);
}

void LogViewer::startLogTail() {
    stopLogTail();
    logWatcher->addPath(openLog.fileName());
}

void LogViewer::logTailToggled(int state) {
    if (state == Qt::Checked) {
        startLogTail();
    } else {
        stopLogTail();
    }
}

}  // Gui
}  // Plow
