package com.breakersoft.plow.http;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.breakersoft.plow.thrift.JobFilterT;
import com.breakersoft.plow.thrift.JobT;
import com.breakersoft.plow.thrift.dao.ThriftJobDao;

/**
 * A controller for exposing the REST interface.
 *
 * Collection URI: /plow/jobs
 * Element URI: /plow/jobs/jobid
 *
 * @author chambers
 *
 */
@Controller
public class RestController {

	private static final Logger logger = LoggerFactory.getLogger(RestController.class);

    @Autowired
    private ThriftJobDao thriftJobDao;

	@RequestMapping(value = "/jobs", method = RequestMethod.GET)
	public void jobs(HttpServletRequest request, HttpServletResponse response)
			throws IOException, TException {
		// TODO: implement filtering via query string.
		serializeOut(response, thriftJobDao.getJobs(new JobFilterT()));
	}

	/**
	 * Serialize a list TBase objects out through an HttpServletResponse.
	 *
	 * @param response
	 * @param items
	 * @throws IOException
	 * @throws TException
	 */
	private void serializeOut(HttpServletResponse response, List<?> items) throws IOException, TException {

		final TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
		final ServletOutputStream stream = response.getOutputStream();
		response.setContentType("application/json");
		stream.print("[\n");
		for (JobT o: thriftJobDao.getJobs(new JobFilterT())) {
			stream.print(serializer.toString(o));
		}
		stream.print("]");
		stream.flush();
	}
}
