package com.example.charging.entity;

/**
 * TODO  PACKAGE_NAME
 *
 * @author WeiTang
 * @date 2023/8/7
 */
public class UserEntity {
    private int id;
    private String name;

    public UserEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "com.example.charging.entity.UserEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
