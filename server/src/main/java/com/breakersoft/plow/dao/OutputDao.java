package com.breakersoft.plow.dao;

import java.util.Map;
import java.util.UUID;

import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Output;

public interface OutputDao {

    Map<String, String> getAttrs(UUID outputId);

    Map<String, String> updateAttrs(UUID outputId, Map<String, String> attrs);

    Map<String, String> setAttrs(UUID outputId, Map<String, String> attrs);

    Output addOutput(Layer layer, String path, Map<String, String> attrs);

}
