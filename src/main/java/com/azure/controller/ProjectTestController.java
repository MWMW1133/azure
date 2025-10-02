package com.azure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectTestController {
    @GetMapping("/projectTest")
    public String projectTest(){ return "projectTest"; }
}

