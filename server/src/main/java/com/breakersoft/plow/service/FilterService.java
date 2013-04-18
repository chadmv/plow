package com.breakersoft.plow.service;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Action;
import com.breakersoft.plow.ActionFull;
import com.breakersoft.plow.Filter;
import com.breakersoft.plow.FilterableJob;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Matcher;
import com.breakersoft.plow.MatcherFull;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.thrift.ActionType;
import com.breakersoft.plow.thrift.MatcherField;
import com.breakersoft.plow.thrift.MatcherType;

public interface FilterService {

    Filter createFilter(Project project, String name);

    Filter getFilter(UUID id);

    void setFilterName(Filter filter, String name);

    void setFilterOrder(Filter filter, int order);

    void increaseFilterOrder(Filter filter);

    void decreaseFilterOrder(Filter filter);

    boolean deleteFilter(Filter filter);

    void filterJob(List<MatcherFull> matchers, FilterableJob job);

    /*
     * Matchers
     */
    Matcher createMatcher(Filter filter, MatcherField field, MatcherType type,
            String value);

    Matcher getMatcher(UUID id);

    boolean deleteMatcher(Matcher matcher);

    List<MatcherFull> getMatchers(Project project);

    boolean matchJob(MatcherFull matcher, FilterableJob job);

    /*
     * Actions
     */

    Action getAction(UUID id);

    Action createAction(Filter filter, ActionType type, String value);

    void deleteAction(Action action);

    void applyAction(ActionFull action, Job job);
}
