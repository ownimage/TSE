package com.ownimage.framework.ditest;

import com.ownimage.framework.ditest.subpackage.GreetingService;
import com.ownimage.framework.ditest.subpackage.PersonService;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GreetingServiceTest extends TestCase {

    @InjectMocks
    private GreetingService underTest;

    @Mock
    private PersonService personService;

    @Before
    public void before() {
    }

    @Test
    public void test_greet() {
        when(personService.name()).thenReturn("James");
        var actual = underTest.greet();
        System.out.println(actual);
    }

}