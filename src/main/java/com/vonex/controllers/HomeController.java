package com.vonex.controllers;

import com.vonex.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
	public String answer() {
		return responseService.getAnswer();
	}
}
