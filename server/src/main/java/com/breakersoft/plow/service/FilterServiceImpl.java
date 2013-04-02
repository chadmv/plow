package com.breakersoft.plow.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.Matcher;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.ActionDao;
import com.breakersoft.plow.dao.FilterDao;
import com.breakersoft.plow.dao.MatcherDao;
import com.breakersoft.plow.thrift.MatcherField;
import com.breakersoft.plow.thrift.MatcherType;

@Service
@Transactional
public class FilterServiceImpl implements FilterService {

	@Autowired
	private FilterDao filterDao;

	@Autowired
	private MatcherDao matcherDao;

	@Autowired
	private ActionDao actionDao;

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

}
