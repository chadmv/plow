
import logging

from plow import client 
from plow.gui.util import ask as ask_dialog

LOGGER = logging.getLogger(__name__)


def dropDepends(items, ask=False, parent=None):
    """ 
    Drop the dependencies a list of items have on others 
    
    :param items: a list of either Jobs/Layers/Tasks
    :param ask: bool - use a dialog to confirm before taking action
    :param parent: parent the "ask" dialog to this widget
    """
    if not items:
        return

    typ = '%ss' % (items[0].__class__.__name__)

    total = len(items)
    item_count = 0
    dep_set = set()

    for item in items:
        deps = item.get_depends_on()
        if deps:
            item_count += 1
            dep_set.update(deps)

    if not dep_set:
        return 

    dep_count = len(dep_set)

    dep_title = "dependencies" if dep_count > 1 else "dependency"

    msg = "Drop %(dep_count)d %(dep_title)s on %(item_count)d " \
          "(out of %(total)d) selected %(typ)s, " \
          "so that they can start running?" % locals()

    if ask and not ask_dialog(msg, "Drop %s?" % dep_title, parent):
        return

    LOGGER.debug('Drop depends: %s', dep_set)

    for dep in dep_set:
        dep.drop()


def launchDependsWizard(items, parent=None):
    """ 
    Handler to launch the Dependency Wizard 

    :param items: a list of either 1 or 2 Jobs/Layers/Tasks
    :param parent: parent the dialog to this widget
    """
    if not items:
        return

    from plow.gui.dialogs.depends import DependencyWizard

    item = items[0]

    if isinstance(item, client.Job):
        jobId = item.id 
    else:
        jobId = item.jobId

    proj = client.get_job_spec(jobId).project

    wizard = DependencyWizard(parent, project=proj)
    wizard.resize(wizard.width(), 600)
    wizard.setSourceObject(item)

    if len(items) > 1:
        wizard.setDestObject(items[1])

    wizard.show()