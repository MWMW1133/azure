package com.azure.jspController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ModalController {

    @GetMapping("/event-modal")
    public String eventModal() {
        // /WEB-INF/views/my-calendar-modal.jsp
        return "my-calendar-modal";
    }

    @GetMapping("/plan")
    public String plan() {
        // /WEB-INF/views/project-plan.jsp
        return "project-plan";
    }
}
