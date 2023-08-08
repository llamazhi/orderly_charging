package com.example.charging.entity;

public class DailyTravelDistance {
    private String id;
    private int distance;

    public DailyTravelDistance(String id, int distance) {
        this.id = id;
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "DailyTravelDistance{" +
                "id='" + id + '\'' +
                ", distance=" + distance +
                '}';
    }
}
