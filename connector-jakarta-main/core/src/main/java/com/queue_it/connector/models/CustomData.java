package com.queue_it.connector.models;

public class CustomData {
    private String key;
    private String value;

    public CustomData(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}