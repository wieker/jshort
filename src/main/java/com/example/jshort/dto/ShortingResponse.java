package com.example.jshort.dto;

public class ShortingResponse {

    private String shortUrl;

    private String shortUrlWithDomain;

    public ShortingResponse() {
    }

    public ShortingResponse(String shortUrl, String shortUrlWithDomain) {
        this.shortUrl = shortUrl;
        this.shortUrlWithDomain = shortUrlWithDomain;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getShortUrlWithDomain() {
        return shortUrlWithDomain;
    }

    public void setShortUrlWithDomain(String shortUrlWithDomain) {
        this.shortUrlWithDomain = shortUrlWithDomain;
    }
}
