from base import Panel, WorkspaceManager

from watch import RenderJobWatchPanel
from tasks import TaskPanel
from cluster import ClusterPanel
from logs import LogsPanel
from nodes import NodePanel
from props import PropertiesPanel
from wrangle import JobWranglerPanel

__all__ = [
    "WorkspaceManager",
    "RenderJobWatchPanel",
    "TaskPanel",
    "ClusterPanel",
    "LogsPanel",
    "NodePanel",
    "PropertiesPanel",
    "JobWranglerPanel"
]

# Register the panels
_panels = [
    ('JobWrangler', JobWranglerPanel),
    ('Render Watch', RenderJobWatchPanel),
    ('Tasks', TaskPanel),
    ('Clusters', ClusterPanel),
    ('Nodes', NodePanel),
    ('Logs', LogsPanel),
    ('Properties', PropertiesPanel),
]

for _p in _panels:
    WorkspaceManager.registerPanelType(*_p)

