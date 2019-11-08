package com.iwscream.demo.controller;


import com.iwscream.demo.model.User;
import com.iwscream.demo.service.userServiceImpl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;

@Controller
@RequestMapping("/")
public class Hello {

    @Autowired
    private UserServiceImpl userService;

    @RequestMapping("hello.php")
    @ResponseBody
    public String sayHello(){
//        System.out.println("hello");
        String[] arr = new String[3];
        arr[1] = "haha";
        arr[2] = "xixi";
        for (User user :userService.selectAll()){
            arr[0] = user.getUsername();
        }
        return Arrays.toString(arr);
    }
}
