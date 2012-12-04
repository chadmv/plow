#ifndef PLOW_GUI_LOGVIEWER_H_
#define PLOW_GUI_LOGVIEWER_H_

#include <QWidget>
#include <QFile>
#include <QTextStream>
#include <QTextCursor>
#include <QTextDocument>
#include <QMap>
#include <QSet>
#include <QStringList>
#include <QTimer>
#include <QFileInfo>
#include <QDateTime>

#include "plow/plow.h"

class QPlainTextEdit;
class QLineEdit;
class QCheckBox;
class QTextStream;
class QFile;
class QTabWidget;


namespace Plow {
namespace Gui {

// FileWatcher
class FileWatcher : public QObject
{
    Q_OBJECT
    Q_PROPERTY(int msec READ interval WRITE setInterval)
 public:
    explicit FileWatcher(QObject *parent = 0);

    inline void addPath(const QString &path) {
        if (!watchedFiles.contains(path))
            watchedFiles.insert(path,
                                QFileInfo(path).lastModified());

        if (!watchedFiles.isEmpty() && !watchTimer->isActive())
            start();
    }

    inline void removePath(const QString &path) {
        watchedFiles.remove(path);
        if (watchedFiles.isEmpty())
            stop();
    }

    inline void removePaths(const QStringList &aList) {
        Q_FOREACH(const QString& path, aList)
            removePath(path);
    }

    inline int interval() const
    { return watchTimer->interval(); }

    inline QStringList files() const
    { return QStringList(watchedFiles.keys()); }

 public slots:
    void start() { watchTimer->start(); }
    void stop() { watchTimer->stop(); }
    void setInterval(int msec) {
        watchTimer->setInterval(msec);
        if (watchTimer->isActive())
            watchTimer->start();
    }

 signals:
    void fileChanged(QString);

 private slots:
    void checkFiles();

 private:
    QMap<QString, QDateTime> watchedFiles;
    QTimer *watchTimer;


};


// LogViewer
class LogViewer : public QWidget
{
    Q_OBJECT
    Q_PROPERTY(int msec READ interval WRITE setInterval)
 public:
    explicit LogViewer(QWidget *parent = 0);

    QString logPath() const;
    QString taskName() const;
    QString taskId() const;

    inline int interval() const
    { return logWatcher->interval(); }

  public slots:
    void setCurrentTask(const QString &taskId);
    void setLogPath(const QString&);
    void findText(const QString&, const QTextCursor& = QTextCursor(),
                  QTextDocument::FindFlags = 0);
    void findPrev();
    void findNext();
    void startLogTail();
    void stopLogTail();

    void setInterval(int msec)
    { logWatcher->setInterval(msec); }

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
    FileWatcher *logWatcher;

    Plow::TaskT currentTask;
    
};


// TabbedLogCollection
class TabbedLogCollection : public QWidget
{
    Q_OBJECT
    Q_PROPERTY(int msec READ interval WRITE setInterval)
 public:
    explicit TabbedLogCollection(QWidget *parent = 0);

    inline int interval() const { return m_interval; }

 public slots:
    void addTask(const QString&);
    void closeTab(int);
    void closeAllTabs();
    void setInterval(int);

 private:
    int m_interval;
    QTabWidget *tabWidget;
    QSet<QString> taskIndex;

};

}  // Gui
}  // Plow

#endif // PLOW_GUI_LOGVIEWER_H_
