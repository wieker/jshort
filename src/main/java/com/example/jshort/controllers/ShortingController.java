package com.example.jshort.controllers;

import com.example.jshort.dto.ShortingRequest;
import com.example.jshort.dto.ShortingResponse;
import com.example.jshort.services.ShortingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

@RestController
public class ShortingController {

    @Autowired
    ShortingService shortingService;

    @RequestMapping(value = "/short", method = RequestMethod.POST)
    public ShortingResponse shortUrl(@RequestBody ShortingRequest shortingRequest,
                                     HttpServletRequest request) {
        if (shortingRequest == null || shortingRequest.getLongUrl() == null) {
            return new ShortingResponse(null, null);
        }
        String shortUrl = shortingService.saveInformation(shortingRequest);
        String shortUrlWithDomain = getURLBase(request) + "/" + shortUrl;
        return new ShortingResponse(shortUrl, shortUrlWithDomain);
    }

    public String getURLBase(HttpServletRequest request) {
        try {
            URL requestURL = new URL(request.getRequestURL().toString());
            String port = requestURL.getPort() == -1 ? "" : ":" + requestURL.getPort();
            return requestURL.getProtocol() + "://" + requestURL.getHost() + port;
        } catch (MalformedURLException e) {
            return null;
        }
    }

}
