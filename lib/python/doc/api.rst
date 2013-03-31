
API Documentation
************************

.. contents::


.. _general:

General
===================

.. autofunction:: plow.get_plow_time
.. autofunction:: plow.is_uuid

.. _project:

Project
===================

.. autoclass:: plow.Project

.. autofunction:: plow.get_project
.. autofunction:: plow.get_projects
.. autofunction:: plow.get_project_by_code
.. autofunction:: plow.get_active_projects
.. autofunction:: plow.create_project
.. autofunction:: plow.set_project_active

.. _folder:

Folder
===================

.. autoclass:: plow.Folder

.. autofunction:: plow.get_folder
.. autofunction:: plow.get_folders
.. autofunction:: plow.create_folder
.. autofunction:: plow.get_job_board
.. autofunction:: plow.set_folder_min_cores
.. autofunction:: plow.set_folder_max_cores
.. autofunction:: plow.set_folder_name
.. autofunction:: plow.delete_folder

.. _job:

Job
===================

.. data:: plow.JobState

	Constants representing the state of a Job 

	.. data:: plow.JobState.INITIALIZE
	.. data:: plow.JobState.RUNNING
	.. data:: plow.JobState.FINISHED

.. autoclass:: plow.Job
.. autoclass:: plow.JobSpec



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

.. autoclass:: plow.Cluster
.. autoclass:: plow.ClusterCounts

.. autofunction:: plow.get_cluster
.. autofunction:: plow.get_clusters
.. autofunction:: plow.get_clusters_by_tag
.. autofunction:: plow.create_cluster
.. autofunction:: plow.delete_cluster
.. autofunction:: plow.lock_cluster
.. autofunction:: plow.set_cluster_tags
.. autofunction:: plow.set_cluster_name
.. autofunction:: plow.set_default_cluster





