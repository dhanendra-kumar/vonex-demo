package com.vonex.controllers;

import com.vonex.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

	@Autowired
	ResponseService responseService;

	@RequestMapping("/")
	public String hello() {
		return "home/index";
	}

	@RequestMapping("/answer")
	@ResponseBody
	public String answer(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		return responseService.getAnswer(userAgent);
	}
}
