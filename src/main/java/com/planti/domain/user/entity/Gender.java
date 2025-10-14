package com.planti.domain.user.entity;


import com.fasterxml.jackson.annotation.JsonCreator;

public enum Gender {
    FEMALE, MALE, UNKNOWN;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Gender from(String value) {
        if (value == null) return null;
        return Gender.valueOf(value.trim().toUpperCase());
    }
}