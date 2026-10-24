package com.example.reusisam.DTOS;

public class LoginDTO {
    private String user; // O el nombre exacto que tenga el DTO en Spring Boot
    private String pass; // O el nombre exacto que tenga el DTO en Spring Boot

    public LoginDTO(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}