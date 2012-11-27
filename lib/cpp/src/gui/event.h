#ifndef PLOW_GUI_EVENT_H
#define PLOW_GUI_EVENT_H

#include <QEvent>
#include <QObject>

#include "plow/plow.h"

namespace Plow {
namespace Gui {


class EventManager: public QObject
{
    Q_OBJECT

public:
    static EventManager* getInstance()
    {
        static EventManager instance;
        return &instance;
    }

public slots:
    void handleJobSelected(const QString& id);
    void customEvent(QEvent* event);

signals:
    void jobSelected(QString id);

private:
    EventManager(QObject* widget = 0);
    EventManager(EventManager const&);
    void operator=(EventManager const&);
};

class JobSelectedEvent: public QEvent
{

public:
    JobSelectedEvent(const Guid& id);
    Guid getJobId() const { return m_jobid; }

private:
    Guid m_jobid;
};


}
}

#endif
