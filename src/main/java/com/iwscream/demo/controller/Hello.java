package com.iwscream.demo.controller;


import com.alibaba.fastjson.JSONObject;
import com.iwscream.demo.model.User;
import com.iwscream.demo.service.userServiceImpl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/")
//, name = "operation about users"
public class Hello {

    private UserServiceImpl userService;

    @Autowired
    public Hello(UserServiceImpl userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "select_all_user.php", method = RequestMethod.GET)
    public String select_all() {
        String result = null;
        StringBuffer arr = new StringBuffer();
        try {
            for (User user : userService.selectAll()) {
                arr.append(user.getUsername()).append(",");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            result = "error!!";
        }
        if (result == null) {
            result = String.valueOf(arr);
        }
        return result;
    }

    @RequestMapping(value = "select_user_id.php", method = RequestMethod.GET)
    public String select_id(@RequestParam Integer id){
        User user = null;
        try{
            user = userService.selectByPrimaryKey(id);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
        return new JSONObject().toJSONString(user);
    }
}
