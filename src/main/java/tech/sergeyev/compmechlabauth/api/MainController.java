package tech.sergeyev.compmechlabauth.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpHeaders;

@RestController
@RequestMapping("/")
public class MainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    @GetMapping
    public String index(HttpHeaders headers) {
        LOGGER.info("GET request received{}", headers.toString());
        return "index";
    }
}
