
Client API Documentation
************************

.. contents::


.. _general:

General
===================

.. autofunction:: plow.client.get_plow_time
.. autofunction:: plow.client.is_uuid

.. data:: plow.client.DependType

    Constants representing dependencies between types

    .. data:: plow.client.DependType.JOB_ON_JOB
    .. data:: plow.client.DependType.LAYER_ON_LAYER
    .. data:: plow.client.DependType.LAYER_ON_TASK
    .. data:: plow.client.DependType.TASK_ON_LAYER
    .. data:: plow.client.DependType.TASK_ON_TASK
    .. data:: plow.client.DependType.TASK_BY_TASK

.. autoclass:: plow.client.DependSpec


.. _project:

Project
===================

.. autoclass:: plow.client.Project

.. autofunction:: plow.client.get_project
.. autofunction:: plow.client.get_projects
.. autofunction:: plow.client.get_project_by_code
.. autofunction:: plow.client.get_active_projects
.. autofunction:: plow.client.create_project
.. autofunction:: plow.client.set_project_active

.. _folder:

Folder
===================

.. autoclass:: plow.client.Folder

.. autofunction:: plow.client.get_folder
.. autofunction:: plow.client.get_folders
.. autofunction:: plow.client.create_folder
.. autofunction:: plow.client.get_job_board
.. autofunction:: plow.client.set_folder_min_cores
.. autofunction:: plow.client.set_folder_max_cores
.. autofunction:: plow.client.set_folder_name
.. autofunction:: plow.client.delete_folder

.. _job:

Job
===================

.. data:: plow.client.JobState

    Constants representing the state of a Job 

    .. data:: plow.client.JobState.INITIALIZE
    .. data:: plow.client.JobState.RUNNING
    .. data:: plow.client.JobState.FINISHED

.. autoclass:: plow.client.Job
.. autoclass:: plow.client.JobSpec

.. autofunction:: plow.client.launch_job
.. autofunction:: plow.client.get_active_job
.. autofunction:: plow.client.get_job
.. autofunction:: plow.client.kill_job
.. autofunction:: plow.client.pause_job
.. autofunction:: plow.client.get_jobs
.. autofunction:: plow.client.get_job_outputs
.. autofunction:: plow.client.set_job_min_cores
.. autofunction:: plow.client.set_job_max_cores

.. autoclass:: plow.client.Output
.. autofunction:: plow.client.get_job_outputs


.. _task:

Task
===================

.. data:: plow.client.TaskState

    Constants representing the state of a Task 

    .. data:: plow.client.TaskState.INITIALIZE
    .. data:: plow.client.TaskState.WAITING
    .. data:: plow.client.TaskState.RUNNING
    .. data:: plow.client.TaskState.DEAD
    .. data:: plow.client.TaskState.EATEN
    .. data:: plow.client.TaskState.DEPEND
    .. data:: plow.client.TaskState.SUCCEEDED

.. autoclass:: plow.client.TaskSpec

.. autoclass:: plow.client.TaskTotals

.. autoclass:: plow.client.Task
.. autofunction:: plow.client.get_task
.. autofunction:: plow.client.get_tasks
.. autofunction:: plow.client.get_task_log_path
.. autofunction:: plow.client.retry_tasks
.. autofunction:: plow.client.eat_tasks
.. autofunction:: plow.client.kill_tasks


.. _layer:

Layer
===================

.. autoclass:: plow.client.LayerSpec

.. autoclass:: plow.client.Layer
.. autofunction:: plow.client.get_layer_by_id
.. autofunction:: plow.client.get_layer
.. autofunction:: plow.client.get_layers
.. autofunction:: plow.client.add_layer_output
.. autofunction:: plow.client.get_layer_outputs
.. autofunction:: plow.client.set_layer_tags
.. autofunction:: plow.client.set_layer_min_cores_per_task
.. autofunction:: plow.client.set_layer_max_cores_per_task
.. autofunction:: plow.client.set_layer_min_ram_per_task
.. autofunction:: plow.client.set_layer_threadable


.. _node:

Node
===================

.. data:: plow.client.NodeState

    Constants representing the state of a Node 

    .. data:: plow.client.NodeState.UP
    .. data:: plow.client.NodeState.DOWN
    .. data:: plow.client.NodeState.REPAIR

.. autoclass:: plow.client.NodeSystem

.. autoclass:: plow.client.Node
.. autofunction:: plow.client.get_node
.. autofunction:: plow.client.get_nodes
.. autofunction:: plow.client.set_node_locked
.. autofunction:: plow.client.set_node_cluster
.. autofunction:: plow.client.set_node_tags


.. _cluster:

Cluster
===================

.. autoclass:: plow.client.Cluster
.. autoclass:: plow.client.ClusterCounts

.. autofunction:: plow.client.get_cluster
.. autofunction:: plow.client.get_clusters
.. autofunction:: plow.client.get_clusters_by_tag
.. autofunction:: plow.client.create_cluster
.. autofunction:: plow.client.delete_cluster
.. autofunction:: plow.client.lock_cluster
.. autofunction:: plow.client.set_cluster_tags
.. autofunction:: plow.client.set_cluster_name
.. autofunction:: plow.client.set_default_cluster

.. _quote:

Quota
===================

.. autoclass:: plow.client.Quota
.. autofunction:: plow.client.get_quota
.. autofunction:: plow.client.get_quotas
.. autofunction:: plow.client.create_quota
.. autofunction:: plow.client.set_quota_size
.. autofunction:: plow.client.set_quota_burst
.. autofunction:: plow.client.set_quota_locked

.. _filters:

Filtering / Matching / Actions
==============================

.. data:: plow.client.MatcherType

    Constants definging match types

    .. data:: plow.client.MatcherType.CONTAINS
    .. data:: plow.client.MatcherType.NOT_CONTAINS
    .. data:: plow.client.MatcherType.IS
    .. data:: plow.client.MatcherType.IS_NOT
    .. data:: plow.client.MatcherType.BEGINS_WITH
    .. data:: plow.client.MatcherType.ENDS_WITH

.. data:: plow.client.MatcherField

    Constants field types to match against 

    .. data:: plow.client.NodeState.JOB_NAME
    .. data:: plow.client.NodeState.PROJECT_CODE
    .. data:: plow.client.NodeState.USER
    .. data:: plow.client.NodeState.ATTR

.. autoclass:: plow.client.Matcher
.. autofunction:: plow.client.create_matcher
.. autofunction:: plow.client.get_matcher
.. autofunction:: plow.client.get_matchers
.. autofunction:: plow.client.delete_matcher

.. data:: plow.client.ActionType

    Constants representing actions to trigger 

    .. data:: plow.client.ActionType.SET_FOLDER
    .. data:: plow.client.ActionType.SET_MIN_CORES
    .. data:: plow.client.ActionType.SET_MAX_CORES
    .. data:: plow.client.ActionType.PAUSE
    .. data:: plow.client.ActionType.STOP_PROCESSING

.. autoclass:: plow.client.Action
.. autofunction:: plow.client.create_action
.. autofunction:: plow.client.get_action
.. autofunction:: plow.client.get_actions
.. autofunction:: plow.client.delete_action

.. autoclass:: plow.client.Filter
.. autofunction:: plow.client.create_filter
.. autofunction:: plow.client.get_filters
.. autofunction:: plow.client.get_filter
.. autofunction:: plow.client.delete_filter
.. autofunction:: plow.client.set_filter_name
.. autofunction:: plow.client.set_filter_order
.. autofunction:: plow.client.increase_filter_order
.. autofunction:: plow.client.decrease_filter_order

