package com.ownimage.framework.ditest.subpackage;

import org.springframework.stereotype.Service;

@Service
public class PersonService {

    public String name() {
        return "John";
    }
}
