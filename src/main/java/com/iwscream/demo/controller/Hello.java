package com.iwscream.demo.controller;


import com.iwscream.demo.model.User;
import com.iwscream.demo.service.AuthenticationServiceImpl.LoginServiceImpl;
import com.iwscream.demo.service.userServiceImpl.UserServiceImpl;
import com.iwscream.demo.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/")
//, name = "operation about users"
public class Hello {

    private UserServiceImpl userService;
    private LoginServiceImpl loginService;

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
        return JsonUtil.o2j(user);
    }

    @RequestMapping(value = "insert_user.php", method = RequestMethod.POST)
    public String insert_user(@RequestBody User user){
        int num = userService.insert(user);
        return Integer.toString(num);
    }

    @RequestMapping(value = "getk", method = RequestMethod.GET)
    public String getK(String k){
        loginService = new LoginServiceImpl();
        return loginService.getKey(k);
    }

    @RequestMapping(value = "setk", method = RequestMethod.GET)
    public String setK(String k, String v){
        loginService = new LoginServiceImpl();
        return loginService.setKey(k, v);
    }

    @RequestMapping(value = "get_session_key", method = RequestMethod.GET)
    public String getSK(String code){
        loginService = new LoginServiceImpl();
        return loginService.getSessionKey(code);
    }
}
