package com.ownimage.framework.ditest.subpackage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;


@Service
@Import({Config.class})
public class GreetApp {

    @Autowired
    private GreetingService greetingService;

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        var app = (GreetApp) context.getBean(GreetApp.class);
        app.greet();
    }

    public void greet() {
         var greeting = greetingService.greet();
        System.out.println(greeting);
    }
}
