

#include "event.h"

namespace Plow {
namespace Gui {

static const int JOB_SELECTED_EVENT_TYPE = 1100;

EventManager::EventManager(QObject* parent):
    QObject(parent)
{

}

void EventManager::customEvent(QEvent* event)
{
    std::cout << "custom event " << std::endl;
}

void EventManager::handleJobSelected(const QString& id)
{
    std::cout << "event manager " << id.toStdString() << std::endl;
    emit jobSelected(id);
}

JobSelectedEvent::JobSelectedEvent(const Guid& id) :
    QEvent((QEvent::Type) JOB_SELECTED_EVENT_TYPE)
{
    m_jobid = id;
}


} // Gui
} // Plow
