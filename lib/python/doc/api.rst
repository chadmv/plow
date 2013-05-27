
Client API Documentation
************************

.. contents::
    :depth: 2


.. _general:

General
===================

Functions
^^^^^^^^^

.. autofunction:: plow.client.get_plow_time
.. autofunction:: plow.client.reconnect
.. autofunction:: plow.client.get_host
.. autofunction:: plow.client.set_host
.. autofunction:: plow.client.is_uuid


.. _project:

Project
===================

Classes
^^^^^^^^^

.. _project_project:

.. autoclass:: plow.client.Project
    :members:

Functions
^^^^^^^^^

.. autofunction:: plow.client.get_project
.. autofunction:: plow.client.get_projects
.. autofunction:: plow.client.get_project_by_code
.. autofunction:: plow.client.get_active_projects
.. autofunction:: plow.client.create_project
.. autofunction:: plow.client.set_project_active
.. autofunction:: plow.client.get_job_board

.. _folder:

Folder
===================

Classes
^^^^^^^^^

.. _folder_folder:

.. autoclass:: plow.client.Folder
    :members:

Functions
^^^^^^^^^

.. autofunction:: plow.client.get_folder
.. autofunction:: plow.client.get_folders
.. autofunction:: plow.client.create_folder
.. autofunction:: plow.client.set_folder_min_cores
.. autofunction:: plow.client.set_folder_max_cores
.. autofunction:: plow.client.set_folder_name
.. autofunction:: plow.client.delete_folder

.. _job:

Job
===================

Constants
^^^^^^^^^

.. data:: plow.client.JobState

    Constants representing the state of a Job 

    .. data:: plow.client.JobState.INITIALIZE
    .. data:: plow.client.JobState.RUNNING
    .. data:: plow.client.JobState.FINISHED

Classes
^^^^^^^^^

.. _job_job:

.. autoclass:: plow.client.Job
    :members:

.. _job_jobspec:

.. autoclass:: plow.client.JobSpec
    :members:

.. _job_jobstats:

.. autoclass:: plow.client.JobStats
    :members:

.. _job_output:

.. autoclass:: plow.client.Output
    :members:

Functions
^^^^^^^^^

.. autofunction:: plow.client.launch_job
.. autofunction:: plow.client.get_active_job
.. autofunction:: plow.client.get_job
.. autofunction:: plow.client.kill_job
.. autofunction:: plow.client.pause_job
.. autofunction:: plow.client.get_jobs
.. autofunction:: plow.client.get_job_outputs
.. autofunction:: plow.client.set_job_min_cores
.. autofunction:: plow.client.set_job_max_cores

.. autofunction:: plow.client.get_job_spec

.. autofunction:: plow.client.get_job_outputs


.. _task:

Task
===================

Constants
^^^^^^^^^

.. data:: plow.client.TaskState

    Constants representing the state of a Task 

    .. data:: plow.client.TaskState.INITIALIZE
    .. data:: plow.client.TaskState.WAITING
    .. data:: plow.client.TaskState.RUNNING
    .. data:: plow.client.TaskState.DEAD
    .. data:: plow.client.TaskState.EATEN
    .. data:: plow.client.TaskState.DEPEND
    .. data:: plow.client.TaskState.SUCCEEDED

Classes
^^^^^^^^^

.. _task_task:

.. autoclass:: plow.client.Task
    :members:

.. _task_spec:

.. autoclass:: plow.client.TaskSpec
    :members:

.. _task_stats:

.. autoclass:: plow.client.TaskStats
    :members:

.. _task_totals:

.. autoclass:: plow.client.TaskTotals
    :members:

Functions
^^^^^^^^^

.. autofunction:: plow.client.get_task_stats

.. autofunction:: plow.client.get_task
.. autofunction:: plow.client.get_tasks
.. autofunction:: plow.client.get_task_log_path
.. autofunction:: plow.client.retry_tasks
.. autofunction:: plow.client.eat_tasks
.. autofunction:: plow.client.kill_tasks


.. _layer:

Layer
===================

Classes
^^^^^^^^^

.. _layer_layer:

.. autoclass:: plow.client.Layer
    :members:

.. _layer_spec:

.. autoclass:: plow.client.LayerSpec
    :members:

.. _layer_stats:

.. autoclass:: plow.client.LayerStats
    :members:

.. _layer_service:

.. autoclass:: plow.client.Service
    :members:

Functions
^^^^^^^^^

.. autofunction:: plow.client.get_services
.. autofunction:: plow.client.create_service
.. autofunction:: plow.client.delete_service
.. autofunction:: plow.client.update_service

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

Constants
^^^^^^^^^

.. data:: plow.client.NodeState

    Constants representing the state of a Node 

    .. data:: plow.client.NodeState.UP
    .. data:: plow.client.NodeState.DOWN
    .. data:: plow.client.NodeState.REPAIR

Classes
^^^^^^^^^

.. _node_node:

.. autoclass:: plow.client.Node
    :members:

.. _node_system:

.. autoclass:: plow.client.NodeSystem
    :members:

Functions
^^^^^^^^^

.. autofunction:: plow.client.get_node
.. autofunction:: plow.client.get_nodes
.. autofunction:: plow.client.set_node_locked
.. autofunction:: plow.client.set_node_cluster
.. autofunction:: plow.client.set_node_tags


.. _cluster:

Cluster
===================

Classes
^^^^^^^^^

.. _cluster_cluster:

.. autoclass:: plow.client.Cluster
    :members:

.. _cluster_counts:

.. autoclass:: plow.client.ClusterCounts
    :members:

Functions
^^^^^^^^^

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

Classes
^^^^^^^^^

.. _quote_quota:

.. autoclass:: plow.client.Quota
    :members:

Functions
^^^^^^^^^

.. autofunction:: plow.client.get_quota
.. autofunction:: plow.client.get_quotas
.. autofunction:: plow.client.create_quota
.. autofunction:: plow.client.set_quota_size
.. autofunction:: plow.client.set_quota_burst
.. autofunction:: plow.client.set_quota_locked

.. _filters:

Filtering / Matching / Actions
==============================

Constants
^^^^^^^^^

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
    .. data:: plow.client.NodeState.USER
    .. data:: plow.client.NodeState.ATTR

.. data:: plow.client.ActionType

    Constants representing actions to trigger 

    .. data:: plow.client.ActionType.SET_FOLDER
    .. data:: plow.client.ActionType.SET_MIN_CORES
    .. data:: plow.client.ActionType.SET_MAX_CORES
    .. data:: plow.client.ActionType.PAUSE
    .. data:: plow.client.ActionType.STOP_PROCESSING

Classes
^^^^^^^^^

.. _filters_matcher:

.. autoclass:: plow.client.Matcher
    :members:

.. _filters_action:

.. autoclass:: plow.client.Action
    :members:

.. _filters_filter:

.. autoclass:: plow.client.Filter
    :members:

Functions
^^^^^^^^^

.. autofunction:: plow.client.create_field_matcher
.. autofunction:: plow.client.create_attr_matcher
.. autofunction:: plow.client.get_matcher
.. autofunction:: plow.client.get_matchers
.. autofunction:: plow.client.delete_matcher

.. autofunction:: plow.client.create_action
.. autofunction:: plow.client.get_action
.. autofunction:: plow.client.get_actions
.. autofunction:: plow.client.delete_action

.. autofunction:: plow.client.create_filter
.. autofunction:: plow.client.get_filters
.. autofunction:: plow.client.get_filter
.. autofunction:: plow.client.delete_filter
.. autofunction:: plow.client.set_filter_name
.. autofunction:: plow.client.set_filter_order
.. autofunction:: plow.client.increase_filter_order
.. autofunction:: plow.client.decrease_filter_order



.. _depends:

Dependencies
==============================

Constants
^^^^^^^^^

.. data:: plow.client.DependType

    Constants representing dependencies between types

    .. data:: plow.client.DependType.JOB_ON_JOB
    .. data:: plow.client.DependType.LAYER_ON_LAYER
    .. data:: plow.client.DependType.LAYER_ON_TASK
    .. data:: plow.client.DependType.TASK_ON_LAYER
    .. data:: plow.client.DependType.TASK_ON_TASK
    .. data:: plow.client.DependType.TASK_BY_TASK

Classes
^^^^^^^^^

.. _depends_depend:

.. autoclass:: plow.client.Depend
    :members:

.. _depends_spec:

.. autoclass:: plow.client.DependSpec
    :members:

Functions
^^^^^^^^^

.. autofunction:: plow.client.get_depends_on_job
.. autofunction:: plow.client.get_job_depends_on
.. autofunction:: plow.client.get_depends_on_layer
.. autofunction:: plow.client.get_layer_depends_on
.. autofunction:: plow.client.get_depends_on_task
.. autofunction:: plow.client.get_task_depends_on
.. autofunction:: plow.client.drop_depend
.. autofunction:: plow.client.reactivate_depend
