package com.ownimage.framework.ditest;

import com.ownimage.framework.ditest.subpackage.Config;
import com.ownimage.framework.ditest.subpackage.GreetApp;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

//@SpringBootApplication
//@Service
//@Import({Config.class})
public class NonSpringApp {

    public static void main(String[] args){
        ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        var app = (GreetApp) context.getBean(GreetApp.class);
        app.greet();
    }
}
