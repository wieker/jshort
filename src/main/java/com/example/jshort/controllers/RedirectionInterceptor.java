package com.example.jshort.controllers;

import com.example.jshort.services.ShortingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectionInterceptor implements HandlerInterceptor {

    private static final String GET_METHOD = "GET";

    @Autowired
    ShortingService shortingService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        String location;
        if (GET_METHOD.equals(request.getMethod())) {
            try {
                String requestURI = request.getRequestURI();
                location = shortingService.getLongUrl(requestURI.substring(1));
            } catch (NoResultException e) {
                return true;
            }
            response.setHeader("Location", location);
            return false;
        }

        return true;
    }
}
