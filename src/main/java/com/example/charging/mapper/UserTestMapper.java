package com.example.charging.mapper;


import com.example.charging.entity.UserEntity;

import java.util.List;

/**
 * dao
 */
public interface UserTestMapper {
 
    List<UserEntity> selectAll();

    UserEntity selectById(int id);

    int add(UserEntity user);
 
    int del(int id);

}