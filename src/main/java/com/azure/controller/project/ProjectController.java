
package com.azure.controller.project;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    @Controller
    @RequiredArgsConstructor
    public class LegacyProjectTasksController {
    @GetMapping("/projects/{id}/tasks")
    public String legacy(@PathVariable Long id) {
    return "redirect:/projects/" + id;
  }
}
}
