package tech.sergeyev.ssoauthserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class CompmechlabAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompmechlabAuthApplication.class, args);
    }

}
