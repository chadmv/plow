#ifndef PLOW_GUI_LOGVIEWER_H_
#define PLOW_GUI_LOGVIEWER_H_

#include <QWidget>
#include <QFile>
#include <QTextStream>
#include <QTextCursor>
#include <QTextDocument>

#include "plow/plow.h"

class QPlainTextEdit;
class QLineEdit;
class QComboBox;
class QCheckBox;
class QTextStream;
class QFileSystemWatcher;
class QFile;

namespace Plow {
namespace Gui {

class LogViewer : public QWidget
{
    Q_OBJECT
 public:
    explicit LogViewer(QWidget *parent = 0);

    QString logPath() const;

 public slots:
    void setCurrentTask(const QString &taskId);
    void setLogPath(const QString&);
    void findText(const QString&, const QTextCursor& = QTextCursor(),
                  QTextDocument::FindFlags = 0);
    void findPrev();
    void findNext();
    void startLogTail();
    void stopLogTail();
    void refreshTasks(const QString&);

 private slots:
    void logUpdated();
    void logTailToggled(int);
    void openLogFile();
    void taskSelected(int);

 private:
    QPlainTextEdit *view;
    QLineEdit *searchLine;
    QCheckBox *logTailCheckbox;
    QComboBox *taskSelector;

    QFile openLog;
    QTextStream logStream;
    QFileSystemWatcher *logWatcher;

    Plow::TaskT currentTask;
    QMap<QString,Plow::TaskT> taskMap;
    
};

}  // Gui
}  // Plow

#endif // PLOW_GUI_LOGVIEWER_H_
