package com.example.charging.mapper;
import com.example.charging.entity.EVTimeComparison;

import java.util.List;


public interface EVTimeComparisonMapper {
    List<EVTimeComparison> selectAll();

    EVTimeComparison selectById(String id);

    int add(EVTimeComparison entity);

    int del(int id);
}
