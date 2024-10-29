package org.example.portfolio.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.example.portfolio.model.Cash;
import org.example.portfolio.model.Holding;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PortfolioServiceTest {

    private static final ObjectMapper mapper = new ObjectMapper();


    private static MockWebServer mockWebServer;
    @Autowired
    private static PortfolioService portfolioService;


    @BeforeClass
    public static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        WebClient.Builder webclientBuilder = WebClient.builder().baseUrl(mockWebServer.url("/").url().toString());
        portfolioService = new PortfolioService(webclientBuilder);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("server.base.uri", () -> mockWebServer.url("/").toString());
    }

    @Test
    public void getHoldingByPortfolio() {
        // Mock data
        Holding h1 = new Holding("AMZN", 1000);
        Holding h2 = new Holding("GOOGL", 2000);

        List<Holding> holdingList = Arrays.asList(h1, h2);


        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .setBody(returnHoldingsList()));


        Mono<List<Holding>> holding = portfolioService.getHoldings("PORTFOLIO_A");

        StepVerifier
                .create(holding)
                .assertNext(entry ->
                {
                    assertEquals(entry, holdingList);
                })
                .verifyComplete();

    }

    @Test
    public void verifyTimeOutWhileQueryingHolding() {
        // Mock data
        Holding h1 = new Holding("AMZN", 1000);
        Holding h2 = new Holding("GOOGL", 2000);

        List<Holding> holdingList = Arrays.asList(h1, h2);


        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .setBody(returnHoldingsList())
                        .setBodyDelay(20, TimeUnit.SECONDS));


        Mono<List<Holding>> holding = portfolioService.getHoldings("PORTFOLIO_A");

        StepVerifier
                .create(holding)
                .expectError(TimeoutException.class)
                .verify();

    }

    @Test
    public void getHoldingError() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(404)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .setBody(returnHoldingsList()));


        Mono<List<Holding>> holding = portfolioService.getHoldings("PORTFOLIO_A");

        StepVerifier
                .create(holding)
                .expectError(PortfolioServiceException.class)
                .verify();

    }

    @Test
    public void getCashByPortfolio() {
        // Mock data
        Cash cash = new Cash(1000.0);

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .setBody(returnCash()));


        Mono<Cash> cashMono = portfolioService.getCash("PORTFOLIO_A");

        StepVerifier
                .create(cashMono)
                .assertNext(entry ->
                {
                    assertEquals(entry, cash);
                })
                .verifyComplete();

    }


    private String returnHoldingsList() {

        // Mock data
        Holding h1 = new Holding("AMZN", 1000);
        Holding h2 = new Holding("GOOGL", 2000);

        List<Holding> holdingListA = Arrays.asList(h1, h2);


        try {
            String list = mapper.writeValueAsString(holdingListA);
            return list;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String returnCash() {

        try {
            return mapper.writeValueAsString(new Cash(1000.0));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }


}



