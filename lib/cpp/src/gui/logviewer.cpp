#include <QPlainTextEdit>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QLineEdit>
#include <QComboBox>
#include <QCheckBox>
#include <QTextBlock>
#include <QLabel>
#include <QScrollBar>
#include <QPushButton>
#include <QAction>
#include <QFileDialog>
#include <QTabWidget>
#include <QDebug>

#include "logviewer.h"

namespace Plow {
namespace Gui {

//
// FileWatcher
//
FileWatcher::FileWatcher(QObject *parent) :
    QObject(parent)
{
    watchTimer = new QTimer(this);
    watchTimer->setInterval(5000);
    connect(watchTimer, SIGNAL(timeout()), this, SLOT(checkFiles()));
}

void FileWatcher::checkFiles() {
    if (watchedFiles.isEmpty())
        return;

    QFileInfo info;
    QString aFile;
    QDateTime mtime;

    QMap<QString, QDateTime>::iterator i;
    for (i = watchedFiles.begin(); i != watchedFiles.end(); ++i) {
        aFile = i.key();
        info.setFile(aFile);
        mtime = info.lastModified();
        if (i.value() != mtime) {
            i.value() = mtime;
            qDebug() << "File modified:" << aFile;
            emit fileChanged(aFile);
        }
    }
}


//
// LogViewer
//
LogViewer::LogViewer(QWidget *parent) :
    QWidget(parent)
{
    QVBoxLayout* mainLayout = new QVBoxLayout(this);
    mainLayout->setObjectName("mainLayout");

    QAction *openAction = new QAction("Open Log File", this);
    openAction->setShortcut(QKeySequence::Open);
    addAction(openAction);

    searchLine = new QLineEdit(this);
    logTailCheckbox = new QCheckBox("Tail log", this);

    QPushButton *findPrevBtn = new QPushButton("<-", this);
    QPushButton *findNextBtn = new QPushButton("->", this);
    findPrevBtn->setFixedSize(26,20);
    findNextBtn->setFixedSize(26,20);

    QHBoxLayout* controlLayout = new QHBoxLayout;
    controlLayout->setSpacing(0);
    controlLayout->addWidget(new QLabel(tr("Find:")));
    controlLayout->addSpacing(4);
    controlLayout->addWidget(searchLine);
    controlLayout->addWidget(findPrevBtn);
    controlLayout->addWidget(findNextBtn);
    controlLayout->addStretch();
    controlLayout->addWidget(logTailCheckbox);

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

    logWatcher = new FileWatcher(this);

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


void LogViewer::setCurrentTask(const QString &taskId) {
    if (taskId.isEmpty() || currentTask.id == taskId.toStdString())
        return;

    Plow::getTaskById(currentTask, taskId.toStdString());

    std::string c_logpath;
    Plow::getTaskLogPath(c_logpath, currentTask);

    QString logpath = QString::fromStdString(c_logpath);
    qDebug() << "Received logpath:" << logpath;

    if (!QFile::exists(logpath)) {
        qDebug() << "Failed to open file" << logpath;
        currentTask.id = "";
        currentTask.name = "";
        return;
    }

    setLogPath(logpath);

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

QString LogViewer::taskName() const {
    return QString::fromStdString(currentTask.name);
}

QString LogViewer::taskId() const {
    return QString::fromStdString(currentTask.id);
}


//
// TabbedLogCollection
//
TabbedLogCollection::TabbedLogCollection(QWidget *parent) :
    QWidget(parent),
    m_interval(15000),
    tabWidget(new QTabWidget(this))
{
    QVBoxLayout *mainLayout = new QVBoxLayout(this);
    mainLayout->addWidget(tabWidget);

    tabWidget->setMovable(false);
    tabWidget->setTabsClosable(true);

    setLayout(mainLayout);

    connect(tabWidget, SIGNAL(tabCloseRequested(int)), this, SLOT(closeTab(int)));
}

void TabbedLogCollection::addTask(const QString &taskId) {
    if (taskIndex.contains(taskId)) {
        for (int i=0; i < tabWidget->count(); ++i) {
            if (qobject_cast<LogViewer*>(tabWidget->widget(i))->taskId() == taskId) {
                tabWidget->setCurrentIndex(i);
                break;
            }
        }
        return;
    }

    LogViewer *logView = new LogViewer(this);
    logView->setInterval(m_interval);
    logView->setCurrentTask(taskId);
    if (logView->taskId().isEmpty()) {
        qDebug() << "Got empty log view. Removing it.";
        delete logView;
        return;
    }

    QString name = logView->taskName();
    int idx = tabWidget->addTab(logView, name);
    tabWidget->setTabToolTip(idx, name);
    tabWidget->setCurrentWidget(logView);
    taskIndex.insert(taskId);
}

void TabbedLogCollection::closeTab(int index) {
    LogViewer *logview = qobject_cast<LogViewer*>(tabWidget->widget(index));
    tabWidget->removeTab(index);
    taskIndex.remove(logview->taskId());
    delete logview;
}

void TabbedLogCollection::closeAllTabs() {
    while (tabWidget->count())
        closeTab(0);
}

void TabbedLogCollection::setInterval(int msec) {
    LogViewer *logview;
    m_interval = msec;
    for (int i=0; i < tabWidget->count(); ++i) {
        logview = qobject_cast<LogViewer*>(tabWidget->widget(i));
        logview->setInterval(m_interval);
    }
}

}  // Gui
}  // Plow
