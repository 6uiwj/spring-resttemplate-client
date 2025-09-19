package com.sparta.springresttemplateclient.entity;

import lombok.Getter;

@Getter
public class User { //엔티티 아님 일반 클래스
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}