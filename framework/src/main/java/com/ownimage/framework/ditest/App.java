package com.ownimage.framework.ditest;

import com.ownimage.framework.ditest.subpackage.GreetApp;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        new GreetApp().greet();
    }

}
