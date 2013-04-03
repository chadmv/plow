
API Documentation
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


.. .. _task:

.. Task
.. ===================


.. .. _layer:

.. Layer
.. ===================


.. .. _node:

.. Node
.. ===================


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





