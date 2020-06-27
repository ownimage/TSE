package com.ownimage.framework.ditest.subpackage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    @Autowired
    private PersonService personService;

    public String greet() {
        return "Hello " + personService.name() + "!";
    }
}
