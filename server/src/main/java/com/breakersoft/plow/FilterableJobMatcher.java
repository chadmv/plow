package com.breakersoft.plow;

import com.breakersoft.plow.thrift.MatcherField;
import com.breakersoft.plow.thrift.MatcherType;

public class FilterableJobMatcher extends MatcherE {

    public MatcherType type;
    public MatcherField field;
    public String value;
    public String attr;
}
