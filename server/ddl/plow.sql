
CREATE TABLE plow.project (
	pk_project UUID NOT NULL PRIMARY KEY,
	str_name VARCHAR(8) NOT NULL,
	str_title VARCHAR(255) NOT NULL
) WITHOUT OIDS;

CREATE TABLE plow.folder (
	pk_folder UUID NOT NULL PRIMARY KEY,
	pk_parent UUID,
	pk_project UUID NOT NULL,
	str_name VARCHAR(96) NOT NULL
) WITHOUT OIDS;

CREATE TABLE plow.job (
	pk_job UUID NOT NULL PRIMARY KEY,
	pk_project UUID NOT NULL,
	pk_folder UUID,
	str_name VARCHAR(160) NOT NULL,
	str_active_name VARCHAR(160),
	str_user VARCHAR(128) NOT NULL,
	int_uid INTEGER NOT NULL,
	int_state SMALLINT NOT NULL DEFAULT 0,
	bool_paused BOOLEAN NOT NULL DEFAULT 'f',
	time_started BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW()),
	time_stopped BIGINT DEFAULT 0
) WITHOUT OIDS;

CREATE TABLE plow.job_stat (
	pk_job UUID NOT NULL PRIMARY KEY,
	int_total_frame_count INTEGER NOT NULL DEFAULT 0,
	int_success_frame_count INTEGER NOT NULL DEFAULT 0,
	int_dead_frame_count INTEGER NOT NULL DEFAULT 0,
	int_eaten_frame_count INTEGER NOT NULL DEFAULT 0,
	int_waiting_frame_count INTEGER NOT NULL DEFAULT 0,
	int_depend_frame_count INTEGER NOT NULL DEFAULT 0
) WITHOUT OIDS;

CREATE table plow.layer (
	pk_layer UUID NOT NULL PRIMARY KEY,
	pk_job UUID NOT NULL,
	str_name VARCHAR(200) NOT NULL,
	str_range TEXT NOT NULL,
	str_command TEXT NOT NULL,
	int_chunk_size INTEGER NOT NULL,
	int_order INTEGER NOT NULL,
	int_min_cores SMALLINT NOT NULL,
	int_max_cores SMALLINT NOT NULL,
	int_min_mem INTEGER NOT NULL
) WITHOUT OIDS;

CREATE TABLE plow.frame (
	pk_frame UUID NOT NULL PRIMARY KEY,
	pk_layer UUID NOT NULL,
	str_alias VARCHAR(255),
	int_number INTEGER NOT NULL,
	int_order INTEGER NOT NULL,
	int_state SMALLINT NOT NULL
) WITHOUT OIDS;

/*-----------------------------------------------------*/

CREATE TABLE plow.cluster (
	pk_cluster UUID NOT NULL PRIMARY KEY,
	str_name VARCHAR(128) NOT NULL
) WITHOUT OIDS;

CREATE TABLE plow.node (
	pk_node UUID NOT NULL PRIMARY KEY,
	pk_cluster UUID NOT NULL,
	str_name VARCHAR(160) NOT NULL,

) WITHOUT OIDS;

/**
 * 
 */
CREATE TABLE plow.node_dsp (
	pk_node_dsp UUID NOT NULL PRIMARY KEY,
	pk_node UUID NOT NULL,
	int_total_cores SMALLINT NOT NULL,
	int_total_mem INTEGER NOT NULL,
	int_idle_cores SMALLINT NOT NULL,
	int_idle_mem INTEGER NOT NULL	
) WITHOUT OIDS;



CREATE TABLE plow.sub (
	pk_sub UUID NOT NULL PRIMARY KEY,
	pk_cluster UUID NOT NULL,
	pk_project UUID NOT NULL,
	int_cores SMALLINT NOT NULL DEFAULT 0,
	int_burst INTEGER NOT NULL DEFAULT 0
) WITHOUT OIDS;

/*-----------------------------------------------------*/

CREATE TABLE plow.proc (
	pk_proc UUID NOT NULL PRIMARY KEY,
	pk_host UUID NOT NULL,
	pk_frame NOT NULL,
	int_cores SMALLINT NOT NULL,
	int_cores_used SMALLINT NOT NULL,
	int_mem INTEGER NOT NULL,
	int_mem_used INTEGER NOT NULL,
	int_mem_high INTEGER NOT NULL,
	bool_unbooked NOT NULL BOOLEAN DEFAULT 'f'
) WITHOUT OIDS;



