package com.breakersoft.plow;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class FrameSet implements Iterable<Integer> {
	
	private final Set<Integer> frames;
	private String range;
	
	public FrameSet(String range) {
		this.range = range;
		this.frames = new LinkedHashSet<Integer>();
	}

	@Override
	public Iterator<Integer> iterator() {
		return frames.iterator();
	}

}
