package com.breakersoft.plow.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.Matcher;
import com.breakersoft.plow.MatcherFull;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.thrift.MatcherField;
import com.breakersoft.plow.thrift.MatcherType;

public interface MatcherDao {

    Matcher create(Filter filter, MatcherField field, MatcherType type,
            String value);

    boolean delete(Matcher matcher);

    void setValue(Matcher matcher, String value);
    void setField(Matcher matcher, MatcherField field);
    void setType(Matcher matcher, MatcherType type);

    void update(Matcher matcher, MatcherField field, MatcherType type,
            String value);

    Matcher get(UUID id);

    List<MatcherFull> getAllFull(Project project);

    MatcherFull getFull(Matcher matcher);

}
