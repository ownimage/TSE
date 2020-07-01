package com.ownimage.framework.ditest.subpackage;

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
    GreetApp greetApp() {
        return new GreetApp();
    }
}
