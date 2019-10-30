package com.iwscream.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class Hello {

    @RequestMapping("hello.php")
//    @ResponseBody
    public String sayHello(){
//        System.out.println("hello");
        return "hello";
    }
}
