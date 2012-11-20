

class BlueprintException(Exception):
    pass

class LayerException(BlueprintException):
    pass

class ArchiveException(BlueprintException):
    pass

class CommandException(BlueprintException):
	
    def __init__(self, message, exitStatus=1, exitSignal=0):
    	super(CommandException, self).__init__(message)
    	self.exitStatus = exitStatus
    	self.exitSignal = exitSignal