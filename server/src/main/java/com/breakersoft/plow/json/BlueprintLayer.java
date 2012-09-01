package com.breakersoft.plow.json;

import com.breakersoft.plow.exceptions.InvalidBlueprintException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BlueprintLayer {

	private transient static final ObjectMapper mapper = new ObjectMapper();
	
	private String name;
	private String range;
	private String[] command;
	private int chunk;
	private int minCores;
	private int maxCores;
	private int minMemory;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRange() {
		return range;
	}
	public void setRange(String range) {
		this.range = range;
	}
	public String[] getCommand() {
		return command;
	}
	public String getCommandAsJson() {
		try {
			return mapper.writeValueAsString(getCommand());
		} catch(Exception e) {
			throw new InvalidBlueprintException(e);
		}
	}
	public void setCommand(String[] command) {
		this.command = command;
	}
	public int getChunk() {
		return chunk;
	}
	public void setChunk(int chunk) {
		this.chunk = chunk;
	}
	public int getMinCores() {
		return minCores;
	}
	public void setMinCores(int minCores) {
		this.minCores = minCores;
	}
	public int getMaxCores() {
		return maxCores;
	}
	public void setMaxCores(int maxCores) {
		this.maxCores = maxCores;
	}
	public int getMinMemory() {
		return minMemory;
	}
	public void setMinMemory(int minMemory) {
		this.minMemory = minMemory;
	}
}