package com.azure.controller; 

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

  // http://localhost:8080/ 또는 /sidebar 로 접속 가능
  @GetMapping({"/", "/sidebar"})
  public String sidebar() {
    return "sidebar"; // -> /WEB-INF/views/sidebar.jsp
  }

  @GetMapping("/topbar")
  public String topbar(){
    return "topbar"; // -> /WEB-INF/views/topbar.jsp
  }

  @GetMapping("/mainbar")
  public String mainbar(){
    return "mainbar"; // -> /WEB-INF/views/mainbar.jsp
  }

  @GetMapping("/meeting")
  public String meeting() {
  return "meeting"; // /WEB-INF/views/meeting.jsp
}
}
