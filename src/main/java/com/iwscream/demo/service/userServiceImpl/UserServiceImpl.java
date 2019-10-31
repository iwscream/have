package com.iwscream.demo.service.userServiceImpl;

import com.iwscream.demo.mapper.UserMapper;
import com.iwscream.demo.model.User;
import com.iwscream.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Override
    public List<User> selectAll() {
        List<User> userList = userMapper.selectAll();
        return userList;
    }
}
