package org.oopscraft.fintics.emp.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("simulate")
public class SimulateController {

    @GetMapping
    public ModelAndView getSimulate(@RequestParam(value = "simulateId", required = false) String simulateId) {
        ModelAndView modelAndView = new ModelAndView("simulate.html");
        return modelAndView;
    }

}
