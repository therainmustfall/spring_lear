package com.saturday.demo;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
@RestController
public class GreetSimpleController
{
    @RequestMapping("/")
    public String index() {
        return "hello world";
    }


}