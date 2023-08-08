package com.example.charging.mapper;

import com.example.charging.entity.DailyTravelDistance;
import com.example.charging.entity.UserEntity;

import java.util.List;

public interface DailyTravelDistanceMapper {
    int add(DailyTravelDistance entity);

    List<DailyTravelDistance> selectById(String id);

}
