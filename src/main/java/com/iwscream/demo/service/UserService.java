package com.iwscream.demo.service;

import com.iwscream.demo.mapper.UserMapper;
import com.iwscream.demo.model.User;

import java.util.List;

public interface UserService extends UserMapper {

    List<User> selectAll();
}
