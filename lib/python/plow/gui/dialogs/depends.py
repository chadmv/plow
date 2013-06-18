
import logging
from itertools import chain
from functools import partial 

from plow import client 
from plow.client import DependType

from plow.gui.manifest import QtCore, QtGui
from plow.gui import common


LOGGER = logging.getLogger(__name__)


# Mapping of DependType constants to 
# metadata for the UI and its actions
DEPEND_TYPES = {
    DependType.JOB_ON_JOB: {
        'label': 'Job on Job',
        'descript': 'Make one job wait on another job',
        'selection': (client.Job, client.Job),
        'callback': client.create_job_on_job_depend,
    },
    DependType.LAYER_ON_LAYER: {
        'label': 'Layer on Layer',
        'descript': 'Make one layer wait on another layer in the same, or another, job',
        'selection': (client.Layer, client.Layer),
        'callback': client.create_layer_on_layer_depend,
    },
    DependType.LAYER_ON_TASK: {
        'label': 'Layer on Task',
        'descript': 'Make one layer wait on a task in the layer of the same, or another, job',
        'selection': (client.Layer, client.Task),
        'callback': client.create_layer_on_task_depend,
    },
    DependType.TASK_ON_LAYER: {
        'label': 'Task on Layer',
        'descript': 'Make one task wait on a layer of the same, or another, job',
        'selection': (client.Task, client.Layer),
        'callback': client.create_task_on_layer_depend,
    },
    DependType.TASK_ON_TASK: {
        'label': 'Task on Task',
        'descript': 'Make one task wait on a task of the same, or another, job.',
        'selection': (client.Task, client.Task),
        'callback': client.create_task_on_task_depend,
    },
    DependType.TASK_BY_TASK: {
        'label': 'Task By Task',
        'descript': 'Each task of a layer should wait on the corresponding task of another layer, one to one',
        'selection': (client.Layer, client.Layer),
        'callback': client.create_task_by_task_depend,
    },
}


class DependencyWizard(QtGui.QWizard):
    """
    A QWizard dialog allowing the user to apply dependencies
    between Jobs/Layers/Tasks
    """
    def __init__(self, parent=None, project=None, **kwargs):
        super(DependencyWizard, self).__init__(parent)
        self.setWindowTitle("Dependency Wizard")
        self.resize(650, self.height())

        self.__src = None
        self.__project = project

        self.__targetsPage = ChooseTargetsPage(project=project, parent=self)
        self.__confirmPage = ConfirmApplyPage(self)

        self.addPage(ChooseTypeDepPage(self))
        self.addPage(self.__targetsPage)
        self.addPage(self.__confirmPage)

    def sourceObject(self):
        return self.__src

    def setSourceObject(self, obj):
        if not isinstance(obj, (client.Job, client.Layer, client.Task)):
            raise ValueError("Source object must be an instance of Job/Layer/Task")

        self.__src = obj
        self.restart()

    @property 
    def dependantObjects(self):
        return self.__targetsPage.getSourceSelection()

    @property 
    def dependsOnObjects(self):
        return self.__targetsPage.getDestSelection()

    def accept(self):
        if self.__confirmPage.applyDeps():
            super(DependencyWizard, self).accept()
            self.restart()


class BaseDepPage(QtGui.QWizardPage):

    def validatePage(self):
        wiz = self.wizard()
        if not wiz:
            return False

        if not wiz.sourceObject():
            return False 

        return super(BaseDepPage, self).validatePage()

    def sourceObject(self):
        wiz = self.wizard()
        if not wiz:
            return None 

        return wiz.sourceObject()

    @property 
    def dependantObjects(self):
        return self.wizard().dependantObjects

    @property 
    def dependsOnObjects(self):
        return self.wizard().dependsOnObjects


class ChooseTypeDepPage(BaseDepPage):
    """
    A Page giving the user a choice of which dependency
    type to apply.
    """
    def __init__(self, *args, **kwargs):
        super(ChooseTypeDepPage, self).__init__(*args, **kwargs)    

        self.setTitle("Choose the type of dependency")

        layout = QtGui.QVBoxLayout(self)
        self.__title = QtGui.QLabel("", self)
        layout.addWidget(self.__title)

        self.__radioGroup = QtGui.QButtonGroup(self)
        self.__radioGroup.setExclusive(True)

        for val, meta in sorted(DEPEND_TYPES.items()):

            btn = QtGui.QRadioButton(meta['label'], self)
            self.__radioGroup.addButton(btn, val)

            label = QtGui.QLabel("<font color='#c3c3c3'>%s</font>" % meta['descript'], self)
            label.setIndent(30)
            font = btn.font()
            font.setPointSize(font.pointSize() - 1)
            label.setFont(font)

            layout.addWidget(btn)
            layout.addWidget(label)

        self.registerField("dependType*", self, "dependType")
        self.__radioGroup.buttonClicked[int].connect(self.completeChanged)

    def initializePage(self):
        src = self.sourceObject()
        typ = src.__class__.__name__.lower() if src else None

        buttons = self.__radioGroup.buttons()
        defaultSelection = False
        for button in buttons:
            text = button.text().lower()
            button.setEnabled(bool(src))
            if src and text.startswith(typ):
                button.setChecked(True)
                break

        if not src:
            msg = "<font color='red'>No Plow object given to apply dependencies</font>"
            self.__title.setText(msg)
            return

        name = src.name 
        txt = "Dependency Options for <strong>%s</strong> %r" % (typ.title(), name)
        self.__title.setText(txt)

    def isComplete(self):
        if self.dependType == -1:
            return False 

        return super(ChooseTypeDepPage, self).isComplete()

    def getDependType(self):
        return self.__radioGroup.checkedId()

    dependType = QtCore.Property(int, fget=getDependType)


class ChooseTargetsPage(BaseDepPage):
    """
    A Page allowing the user to pick the actual dependant and
    dependsOn items
    """
    def __init__(self, parent=None, project=None, **kwargs):
        super(ChooseTargetsPage, self).__init__(parent)

        self.__project = project

        self.setTitle("Apply the dependency")

        layout = QtGui.QVBoxLayout(self)
        layout.setContentsMargins(2, 0, 2, 0)

        self.__errText = QtGui.QLabel("", self)

        self.group1 = group1 = QtGui.QGroupBox("Dependant Item", self)
        groupLayout1 = QtGui.QVBoxLayout(group1)
        groupLayout1.setContentsMargins(0, 0, 0, 0)
        self.__sourceSelector = src = common.job.JobColumnWidget(project=project, parent=self)
        src.setSingleSelections(True)
        groupLayout1.addWidget(src)

        self.group2 = group2 = QtGui.QGroupBox("Item Depends On", self)
        groupLayout2 = QtGui.QVBoxLayout(group2)
        groupLayout2.setContentsMargins(0, 0, 0, 0)
        self.__destSelector = dst = common.job.JobColumnWidget(project=project, parent=self)
        groupLayout2.addWidget(self.__destSelector)

        layout.addWidget(self.__errText)
        layout.addWidget(group1)
        layout.addWidget(group2)

        self.registerField("sourceSelection*", self, "sourceSelection")
        self.registerField("destSelection*", self, "destSelection")

    def initializePage(self):
        depType = self.field("dependType")

        meta = DEPEND_TYPES[depType]
        subtitle = meta['descript']
        self.setSubTitle(subtitle) 

        srcType, dstType = meta['selection']
        self.group1.setTitle("Dependant %s" % srcType.__name__)
        self.group2.setTitle("Depends On %s" % dstType.__name__)

        self.__errText.clear()

        src = self.__sourceSelector
        self.__initSelector(src)

        dst = self.__destSelector
        self.__initSelector(dst)

        if depType == DependType.JOB_ON_JOB:
            src.setLayersEnabled(False)
            dst.setLayersEnabled(False)

    def __initSelector(self, selector):
        src = self.sourceObject()
        name = src.name
        
        isDest = selector is self.__destSelector 

        if isinstance(src, client.Job):
            if isDest:
                name = src.username
            selector.setJobFilter(name, selectFirst=not isDest)
            return

        job = src.get_job()
        selector.setJobFilter(job.name)

        if isinstance(src, client.Layer):
            if not isDest:
                selector.setLayerFilter(name)
            return

        if isinstance(src, client.Task):
            layer = client.get_layer_by_id(src.layerId)
            selector.setLayerFilter(layer.name)
            if not isDest:
                selector.setTaskFilter(name)

    def validatePage(self):
        src = self.sourceSelection 
        dst = self.destSelection

        errText = self.__errText
        errText.clear()

        if not src or not dst:
            errText.setText("<font color=red>Both dependant and "\
                            "target selections are required</font>")
            return False

        if src[0] in dst:
            errText.setText("<font color=red>Dependant item cannot "\
                            "be set to depend on itself</font>")
            return False

        depType = self.field("dependType")
        srcType, dstType = DEPEND_TYPES[depType]['selection']

        for s in src:
            if not isinstance(s, srcType):
                errText.setText("<font color=red>Dependant item must be a %s</font>" % srcType.__name__)
                return False

        for d in dst:
            if not isinstance(d, dstType):
                errText.setText("<font color=red>'DependsOn' items must be a %s</font>" % dstType.__name__)
                return False

        return super(ChooseTargetsPage, self).validatePage()

    def getSourceSelection(self):
        sel = self.__sourceSelector.getSelection()
        return sel

    sourceSelection = QtCore.Property(list, fget=getSourceSelection)

    def getDestSelection(self):
        sel = self.__destSelector.getSelection()
        return sel

    destSelection = QtCore.Property(list, fget=getDestSelection)


class ConfirmApplyPage(BaseDepPage):
    """
    A page that confirms the dependencies that are about to 
    be applies, and then actually performs the actions.
    """
    def __init__(self, *args, **kwargs):
        super(ConfirmApplyPage, self).__init__(*args, **kwargs)    

        self.setTitle("Confirming dependency")

        self.setSubTitle("Please review the actions that are about to be " \
                         "taken, and then hit 'Finish' to apply them.")

        self.__depOpts = []

        self.__text = text = QtGui.QPlainTextEdit(self)
        text.setReadOnly(True)

        layout = QtGui.QVBoxLayout(self)
        layout.addWidget(self.__text)

    def initializePage(self):
        text = self.__text 
        text.clear()

        deps = self.dependantObjects
        depsOn = self.dependsOnObjects

        depType = self.field("dependType")
        meta = DEPEND_TYPES[depType]

        label = meta['label']
        descript = meta['descript']
        srcClass, depClass = meta['selection']
        callback = meta['callback']

        text = self.__text

        jobIds = set()
        jobs = {}

        for d in chain(deps, depsOn[0:1]):
            if isinstance(d, client.Job):
                jobs[d.id] = d
            else:
                jobIds.add(d.jobId)

        for job in client.get_jobs(jobIds=jobIds):
            jobs[job.id] = job

        opts = [0] * (len(deps) * len(depsOn))

        i = 0
        for dep in deps:

            try:
                job = jobs[dep.jobId]
                msg = "\nMake %s : %s depend upon:" % (job, dep)
            except AttributeError:
                msg = "\nMake %s depend upon:" % dep
            
            text.appendPlainText(msg)

            for depOn in depsOn:

                try:
                    job = jobs[depOn.jobId]
                    msg = "\t%s : %s" % (job, depOn)
                except AttributeError:
                    msg = "\t%s" % depOn

                text.appendPlainText(msg)

                opts[i] = partial(callback, dep, depOn)
                i += 1
            
        self.__depOpts = opts

    def applyDeps(self):
        size = len(self.__depOpts)
        progress = QtGui.QProgressDialog("Applying dependencies...", 
                                         "Cancel", 0, size, self)

        progress.setMinimumDuration(2)
        completed = True

        for i, callback in enumerate(self.__depOpts):
            progress.setValue(i)

            if progress.wasCanceled():
                completed = False
                break

            try:
                callback()

            except Exception, e:
                QtGui.QMessageBox.warning(self, "Error applying dependencies", str(e))
                LOGGER.warn(e)
                completed = False
                break

            if i % 4 == 0:
                QtGui.qApp.processEvents()

        progress.setValue(size)
        return completed 


