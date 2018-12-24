package com.example.jshort.controllers;

/*
    Author: Kirill Abramovich
*/

import com.example.jshort.dto.ShortingRequest;
import com.example.jshort.dto.ShortingResponse;
import com.example.jshort.dto.StatRequest;
import com.example.jshort.dto.StatResponse;
import com.example.jshort.services.StatService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@RestController
public class StatController {

    @Autowired
    private StatService statService;

    @RequestMapping(value = "/getStat", method = RequestMethod.POST)
    public StatResponse shortUrl(@RequestBody StatRequest statRequest,
                                 HttpServletRequest request) {
        Long counter = statService.getStatistics(
                statRequest.getShortUrl(),
                parseDateTime(statRequest.getFromDate()),
                parseDateTime(statRequest.getToDate())
        );
        StatResponse statResponse = new StatResponse();
        statResponse.setCounter((int) counter.intValue());
        return statResponse;
    }

    private DateTime parseDateTime(String dateTime) {
        String pattern = "dd/MM/yyyy HH:mm";
        if (dateTime == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);

        try {
            return new DateTime(formatter.parse(dateTime));
        } catch (Exception e) {
            return null;
        }
    }

}
