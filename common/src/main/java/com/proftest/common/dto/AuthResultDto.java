package com.proftest.common.dto;

import java.io.Serializable;

public class AuthResultDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String token;
    private UserDto user;

    public AuthResultDto() {}

    public AuthResultDto(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }
}
