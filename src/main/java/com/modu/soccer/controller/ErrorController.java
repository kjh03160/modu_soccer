package com.modu.soccer.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorController {

	@GetMapping("/api/error")
	public void error(HttpServletRequest request) throws Throwable {
		throw (Throwable) request.getAttribute("error");
	}
}
