from base import Panel, WorkspaceManager

from watch import RenderJobWatchPanel
from tasks import TaskPanel
from cluster import ClusterPanel
from logs import LogsPanel
from nodes import NodePanel
from props import PropertiesPanel
from wrangle import JobWranglerPanel
from layers import LayerPanel

__all__ = [
    "WorkspaceManager",
    "RenderJobWatchPanel",
    "LayerPanel",
    "TaskPanel",
    "ClusterPanel",
    "LogsPanel",
    "NodePanel",
    "PropertiesPanel",
    "JobWranglerPanel",
]

# Register the panels
_panels = [
    ('JobWrangler', JobWranglerPanel),
    ('Render Watch', RenderJobWatchPanel),
    ('Layers', LayerPanel),
    ('Tasks', TaskPanel),
    ('Logs', LogsPanel),
    ('Properties', PropertiesPanel),
    ('Clusters', ClusterPanel),
    ('Nodes', NodePanel),
]

for _p in _panels:
    WorkspaceManager.registerPanelType(*_p)

