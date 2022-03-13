package tech.sergeyev.compmechlabauth.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/")
public class MainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    @GetMapping
    public ModelAndView index(ModelAndView modelAndView) {
        LOGGER.info("GET request received");
        modelAndView.setViewName("index");
        return modelAndView;
    }
}
