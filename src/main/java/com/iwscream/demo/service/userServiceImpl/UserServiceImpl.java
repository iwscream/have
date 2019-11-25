package com.iwscream.demo.service.userServiceImpl;

import com.iwscream.demo.mapper.UserMapper;
import com.iwscream.demo.model.User;
import com.iwscream.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteByPrimaryKey(Integer userId) {
        return userMapper.deleteByPrimaryKey(userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int insert(User record) {
        return userMapper.insert(record);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User selectByPrimaryKey(Integer userId) {
        return userMapper.selectByPrimaryKey(userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<User> selectAll() {
        return userMapper.selectAll();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int updateByPrimaryKey(User record) {
        return userMapper.updateByPrimaryKey(record);
    }
}
