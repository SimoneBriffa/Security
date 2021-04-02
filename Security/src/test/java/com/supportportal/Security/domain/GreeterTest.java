package com.supportportal.Security.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreeterTest {

    Greeter greeter;

    @BeforeEach
    public void setUp(){
        greeter = new Greeter();
    }

    @Test
    @DisplayName("Must return Hello Simone")
    public void testMultiply() {
        assertEquals("Hello Simone", greeter.greet("Simone"),
                "Must return Hello Simone");
    }

    @Test
    public void testTrim(){
        assertEquals("Hello Simone", greeter.greet("   Simone    "),
                "Must return Hello Simone");
    }

    @Test
    public void testCapitalize(){
        assertEquals("Hello Simone", greeter.greet("simone"),
                "Must return Hello Simone");
    }



}