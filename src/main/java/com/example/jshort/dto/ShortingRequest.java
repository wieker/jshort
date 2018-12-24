package com.example.jshort.dto;

public class ShortingRequest {

    private String longUrl;

    public ShortingRequest() {
    }

    public ShortingRequest(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }
}
