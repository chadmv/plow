package com.breakersoft.plow.http;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	/**
	 * Simply selects the home view to render by returning its name.
	 * @throws IOException
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public void home(HttpServletResponse response) throws IOException {
		response.getOutputStream().write("Welcome to plow\n".getBytes());
	}

}
