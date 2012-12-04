#ifndef PLOW_GUI_LOGVIEWER_H_
#define PLOW_GUI_LOGVIEWER_H_

#include <QWidget>
#include <QFile>
#include <QTextStream>
#include <QTextCursor>
#include <QTextDocument>
#include <QSet>

#include "plow/plow.h"

class QPlainTextEdit;
class QLineEdit;
class QCheckBox;
class QTextStream;
class QFileSystemWatcher;
class QFile;
class QTabWidget;

namespace Plow {
namespace Gui {

// LogViewer
class LogViewer : public QWidget
{
    Q_OBJECT
 public:
    explicit LogViewer(QWidget *parent = 0);

    QString logPath() const;
    QString taskName() const;
    QString taskId() const;

  public slots:
    void setCurrentTask(const QString &taskId);
    void setLogPath(const QString&);
    void findText(const QString&, const QTextCursor& = QTextCursor(),
                  QTextDocument::FindFlags = 0);
    void findPrev();
    void findNext();
    void startLogTail();
    void stopLogTail();

 private slots:
    void logUpdated();
    void logTailToggled(int);
    void openLogFile();

 private:
    QPlainTextEdit *view;
    QLineEdit *searchLine;
    QCheckBox *logTailCheckbox;

    QFile openLog;
    QTextStream logStream;
    QFileSystemWatcher *logWatcher;

    Plow::TaskT currentTask;
    
};


// TabbedLogCollection
class TabbedLogCollection : public QWidget
{
    Q_OBJECT
 public:
    explicit TabbedLogCollection(QWidget *parent = 0);

 public slots:
    void addTask(const QString&);
    void closeTab(int);
    void closeAllTabs();

 private:
    QTabWidget *tabWidget;
    QSet<QString> taskIndex;

};

}  // Gui
}  // Plow

#endif // PLOW_GUI_LOGVIEWER_H_
