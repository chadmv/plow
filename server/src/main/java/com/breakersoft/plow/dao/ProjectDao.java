package com.breakersoft.plow.dao;

import com.breakersoft.plow.Project;

public interface ProjectDao {

	/**
	 * 
	 * @param name
	 * @param title
	 * @return
	 */
	Project create(String name, String title);
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	Project get(String name);
	
	
}
