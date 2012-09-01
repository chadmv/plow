package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Frame;
import com.breakersoft.plow.Layer;

public interface FrameDao {
	
	Frame create(Layer layer, int number, int order);

	Frame get(Layer layer, int number);
	
	Frame get(UUID id);
}
