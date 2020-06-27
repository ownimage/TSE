package com.ownimage.framework.ditest.subpackage;

import com.ownimage.framework.ditest.App;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public GreetingService greetingService() {
        return new GreetingService();
    }

    @Bean
    public PersonService personService() {
        return new PersonService();
    }

    @Bean
    App app() {
        return new App();
    }
}
