package com.example.bazaarstore.model;

public enum Role {
    USER("user"),
    ADMIN("admin");

    private String value;

    Role(String value) {
        this.value = value;
    }
}
