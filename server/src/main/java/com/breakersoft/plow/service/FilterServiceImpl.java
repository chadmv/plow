package com.breakersoft.plow.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Action;
import com.breakersoft.plow.ActionFull;
import com.breakersoft.plow.Filter;
import com.breakersoft.plow.FilterableJob;
import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Matcher;
import com.breakersoft.plow.MatcherFull;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.ActionDao;
import com.breakersoft.plow.dao.FilterDao;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.MatcherDao;
import com.breakersoft.plow.exceptions.FilterException;
import com.breakersoft.plow.thrift.ActionType;
import com.breakersoft.plow.thrift.MatcherField;
import com.breakersoft.plow.thrift.MatcherType;

@Service
@Transactional
public class FilterServiceImpl implements FilterService {

    private static final Logger logger = LoggerFactory.getLogger(FilterServiceImpl.class);

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private MatcherDao matcherDao;

    @Autowired
    private ActionDao actionDao;

    @Autowired
    private JobDao jobDao;

    @Autowired
    private FolderDao folderDao;

    @Override
    public Filter createFilter(Project project, String name) {
        return filterDao.create(project, name);
    }

    @Override
    @Transactional(readOnly=true)
    public Filter getFilter(UUID id) {
        return filterDao.get(id);
    }

    @Override
    public void setFilterName(Filter filter, String name) {
        filterDao.setName(filter, name);
    }

    @Override
    public void setFilterOrder(Filter filter, int order) {
        filterDao.setOrder(filter, order);
    }

    @Override
    public void increaseFilterOrder(Filter filter) {
        filterDao.increaseOrder(filter);
    }

    @Override
    public void decreaseFilterOrder(Filter filter) {
        filterDao.decreaseOrder(filter);
    }

    @Override
    public boolean deleteFilter(Filter filter) {
        return filterDao.delete(filter);
    }

    @Override
    public Matcher createMatcher(Filter filter, MatcherField field, MatcherType type, String value) {
        return matcherDao.create(filter, field, type, value);
    }

    @Override
    public boolean deleteMatcher(Matcher matcher) {
        return matcherDao.delete(matcher);
    }

    @Override
    @Transactional(readOnly=true)
    public Matcher getMatcher(UUID id) {
        return matcherDao.get(id);
    }

    @Override
    @Transactional(readOnly=true)
    public Action getAction(UUID id) {
        return actionDao.get(id);
    }

    @Override
    public Action createAction(Filter filter, ActionType type, String value) {
        return actionDao.create(filter, type, value);
    }

    @Override
    public void deleteAction(Action action) {
        actionDao.delete(action);
    }

    @Override
    public List<MatcherFull> getMatchers(Project project) {
        return matcherDao.getAllFull(project);
    }

    public boolean matchJob(MatcherFull matcher, FilterableJob job) {

         // Get the value to compare

         String comp = "";
         boolean match = false;

         switch (matcher.field) {

         case JOB_NAME:
             comp = job.getName();
             break;
         case USER:
             comp = job.username;
             break;
         case ATTR:
             comp = job.attrs.get(matcher.attr);
             break;
         default:
             throw new FilterException("Invalid matcher field: " + matcher.field.toString());
         }

         if (comp == null) {
             return false;
         }

         // Try to make a match.

         switch (matcher.type) {

         case CONTAINS:
             match = comp.contains(matcher.value);
             break;
         case NOT_CONTAINS:
             match = !comp.contains(matcher.value);
             break;
         case IS:
             match = comp.equals(matcher.value);
             break;
         case IS_NOT:
             match = !comp.equals(matcher.value);
             break;
         case BEGINS_WITH:
             match = comp.startsWith(matcher.value);
             break;
         case ENDS_WITH:
             match = comp.endsWith(matcher.value);
             break;
         default:
             throw new FilterException("Invalid matcher type: " + matcher.type.toString());
         }

         return match;
    }

    @Override
    public void filterJob(List<MatcherFull> matchers, FilterableJob job) {

        for (MatcherFull matcher: matchers) {

            boolean match = matchJob(matcher, job);

            if (match) {
                // Returns true if we should continue matching.
                if (!applActions(matcher, job)) {
                    return;
                }
            }
        }
    }

    public boolean applActions(Matcher matcher, Job job) {

        List<ActionFull> actions = actionDao.getAllFull(matcher);
        logger.debug("Applying {} actions to job: {}", actions.size(), job.getName());

        for (ActionFull action: actions) {
            if (action.type.equals(ActionType.STOP_PROCESSING)) {
                return false;
            }
            applyAction(action, job);
        }

        return true;
    }

    @Override
    public void applyAction(ActionFull action, Job job) {

        switch (action.type) {

        case SET_FOLDER:

            try {
                Folder folder = folderDao.get(action.valueAsUUID());
                logger.info("Moving {} into folder {}", job, folder);
                jobDao.updateFolder(job, folder);
            } catch (Exception e) {
                logger.debug("Failed to move {} into folder, unexpected " + e, e);
            }
            break;

        case SET_MIN_CORES:
            logger.info("Setting {} to min cores {}", job, action.valueAsInt());
            jobDao.setMinCores(job, action.valueAsInt());
            break;

        case SET_MAX_CORES:
            logger.info("Setting {} to max cores {}", job, action.valueAsInt());
            jobDao.setMaxCores(job, action.valueAsInt());
            break;

        case PAUSE:
            logger.info("Setting {} to paused: {}", job, action.value);
            jobDao.setPaused(job, action.valueAsBool());
            break;

        case STOP_PROCESSING:
            break;

        default:
            throw new FilterException("Invalid action type: " + action.type.toString());
        }
    }
}
