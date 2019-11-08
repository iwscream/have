package com.iwscream.demo.service;

import com.iwscream.demo.mapper.UserMapper;
import com.iwscream.demo.model.User;

import java.util.ArrayList;

public interface UserService extends UserMapper {

    ArrayList<User> selectAll();
}
