package com.breakersoft.plow.thrift;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.HttpRequestHandler;

public class ThriftServlet extends TServlet implements HttpRequestHandler {

	private static final long serialVersionUID = -427264249632919150L;

	@Autowired
	public ThriftServlet(RpcService.Iface service) {
		super(new RpcService.Processor<RpcService.Iface>(service), new TJSONProtocol.Factory());
	}

	@Override
	public void handleRequest(HttpServletRequest req, HttpServletResponse rsp)
			throws ServletException, IOException {
		doPost(req, rsp);
	}
}
