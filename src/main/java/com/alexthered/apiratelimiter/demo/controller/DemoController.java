package com.alexthered.apiratelimiter.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/demo")
public class DemoController {

  // a demo endpoint
  @RequestMapping(method = RequestMethod.GET)
  public String getDemoString() {

    return "Hello! All is well";
  }
}
