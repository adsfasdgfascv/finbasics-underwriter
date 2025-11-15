package com.finbasics.model;


public class User {
    
    private Integer id;
    private String username;
    private String passwordHash;

    public Integer getId() {
        return id;
    }

    public void setID(Integer id){
        this.id = id;
    }

    public String getusername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getPasswordHash(){
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash){
        this.passwordHash = passwordHash;
    }
 //removed role becuase this is only for employee/company use 
 // papers are processed by the employees aswell
 

}
