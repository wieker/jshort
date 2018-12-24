package com.example.jshort;

import com.example.jshort.dto.ShortingRequest;
import com.example.jshort.dto.ShortingResponse;
import com.example.jshort.dto.StatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.testng.Assert.assertEquals;

public class FunctionalTestIT {
    private static String shortUrlFirst;
    private static String shortUrlSecond;

    @Test
    public void testShortening() throws Exception {
        shortUrlFirst = shortUrl("https://kernel.org", "1");
        shortUrlFirst = shortUrl("https://kernel.org", "1");
        shortUrlSecond = shortUrl("https://microsoft.com", "2");
    }

    private String shortUrl(String longUrl, String expectedShortUrl) {
        return given()
                .contentType("application/json")
                .body(new ShortingRequest(longUrl))
                .expect()
                .body("shortUrl", equalTo(expectedShortUrl))
                .when()
                .post("/short")
                .body().path("shortUrl");
    }

    @Test(dependsOnMethods = {"testShortening"})
    public void checkSaved() throws Exception {
        checkLocation(shortUrlFirst, "https://kernel.org");
        checkLocation(shortUrlSecond, "https://microsoft.com");
    }

    private void checkLocation(String shortUrl, String expectedLongUrl) throws IOException {
        CloseableHttpResponse response = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080/" + shortUrl));
        String location = null;
        for (Header header : response.getAllHeaders()) {
            if (header.getName().equalsIgnoreCase("Location")) {
                location = header.getValue();
                break;
            }
        }
        assertEquals(location, expectedLongUrl);
    }

    private String sendShortingRequest(String requestPayload) throws IOException {
        HttpPost postRequest = new HttpPost("http://localhost:8080/short");
        postRequest.setEntity(new StringEntity(requestPayload));
        postRequest.setHeader("Content-type", "application/json");
        CloseableHttpResponse response = HttpClients.createDefault().execute(postRequest);
        String body = EntityUtils.toString(response.getEntity());
        ObjectMapper objectMapper = new ObjectMapper();
        ShortingResponse json = objectMapper.readValue(body, ShortingResponse.class);
        return json.getShortUrl();
    }

    @Test(dependsOnMethods = {"checkSaved"})
    public void multiParallel() throws Exception {
        int threadPoolSize = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(threadPoolSize);
        for (int i = 0; i < threadPoolSize; i ++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String url = "randomUrl" + Math.random();
                    ShortingRequest shortingRequest = new ShortingRequest();
                    shortingRequest.setLongUrl(url);
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String requestPayload = objectMapper.writeValueAsString(shortingRequest);
                        String sh = sendShortingRequest(requestPayload);
                        checkLocation(sh, url);
                    } catch (Exception e) {
                        System.out.println(e);
                        e.printStackTrace();
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            }).start();
        }
        countDownLatch.await();
    }


    @Test(dependsOnMethods = {"multiParallel"})
    public void checkStat() throws Exception {
        Thread.sleep(10 * 1000);
        compareStat(shortUrlFirst, null, null, 1);
        compareStat(shortUrlSecond, "01/10/2040 00:00", null, 0);
        compareStat(shortUrlSecond, null, "01/10/1980 00:00", 0);
        compareStat(shortUrlSecond, null, "01/10/2040 00:00", 1);
        compareStat(shortUrlSecond, "01/10/1980 00:00", null,1);
        checkLocation(shortUrlFirst, "https://kernel.org");
        Thread.sleep(10 * 1000);
        compareStat(shortUrlFirst, null, null, 2);
    }

    private void compareStat(String shortUrl, String fromDate, String toDate, int expectedResult) throws InterruptedException {
        StatRequest statRequest = new StatRequest();
        statRequest.setShortUrl(shortUrl);
        statRequest.setFromDate(fromDate);
        statRequest.setToDate(toDate);
        given()
                .contentType("application/json")
                .body(statRequest)
                .expect()
                .body("counter", equalTo(expectedResult))
                .when()
                .post("/getStat")
                .body().path("counter");
    }


    @Test
    public void negativeLocation() throws Exception {
        check404("", null);
        check404("unknown", null);
    }

    private void check404(String shortUrl, String expectedLongUrl) throws IOException {
        CloseableHttpResponse response = HttpClients.createDefault().execute(new HttpGet("http://localhost:8080/" + shortUrl));
        String location = null;
        assertEquals(response.getStatusLine().getStatusCode(), 404);
        for (Header header : response.getAllHeaders()) {
            if (header.getName().equalsIgnoreCase("Location")) {
                location = header.getValue();
                break;
            }
        }
        assertEquals(location, expectedLongUrl);
    }
}
