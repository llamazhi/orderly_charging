package com.example.charging.mapper;

import com.example.charging.entity.LoadComparison;

import java.util.List;

public interface LoadComparisonMapper {
    List<LoadComparison> selectAll();

    LoadComparison selectById(String id);

    int add(LoadComparison entity);

    int del(int id);
}
