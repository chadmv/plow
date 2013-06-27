
from plow.gui.manifest import QtCore, QtGui


def ask(msg, title='Confirm?', parent=None):
    ret = QtGui.QMessageBox.question(parent, title, msg,
                                     QtGui.QMessageBox.Ok | QtGui.QMessageBox.Cancel,
                                     QtGui.QMessageBox.Cancel )
   
    return ret == QtGui.QMessageBox.Ok


def showErrorList(errors, msg=None, title=None, parent=None):
	if not msg:
		msg = 'One or more errors occured'

	if not title:
		title = 'Errors occured'

	dialog = QtGui.QMessageBox(parent)
	dialog.setWindowTitle(title)
	dialog.setText(msg)
	dialog.setIcon(dialog.Warning)

	dialog.setDetailedText('\n'.join(errors))

	dialog.exec_()
