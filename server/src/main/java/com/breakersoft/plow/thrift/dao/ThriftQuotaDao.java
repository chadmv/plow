package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.thrift.QuotaFilterT;
import com.breakersoft.plow.thrift.QuotaT;

public interface ThriftQuotaDao {

	List<QuotaT> getQuotas(QuotaFilterT filter);

	QuotaT getQuota(UUID id);

}
