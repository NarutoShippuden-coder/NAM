package org.example.portfolio.controller;


import io.netty.handler.timeout.TimeoutException;
import org.example.portfolio.model.Cash;
import org.example.portfolio.model.Holding;
import org.example.portfolio.model.Portfolio;
import org.example.portfolio.service.PortfolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

public class PortfolioControllerTest {

    @Mock
    private PortfolioService portfolioService;

    @InjectMocks
    private PortfolioController portfolioController;

    @Value("${timeout.duration.seconds}")
    private int TIMEOUT_IN_SECONDS;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetPortfoliosByStock() {
        //prepare dummy data
        Portfolio p1 = new Portfolio("PORTFOLIO_A", false);
        Portfolio p2 = new Portfolio("PORTFOLIO_B", false);
        Portfolio p3 = new Portfolio("PORTFOLIO_C", true);

        List<Portfolio> portfolios = Arrays.asList(p1, p2, p3);

        Holding h1 = new Holding("AMZN", 1000);
        Holding h2 = new Holding("GOOGL", 2000);
        Holding h3 = new Holding("MSFT", 1000);

        when(portfolioService.getAllPortfolios()).thenReturn(Mono.just(portfolios));
        when(portfolioService.getHoldings("PORTFOLIO_A")).thenReturn(Mono.just(Arrays.asList(h1)));
        when(portfolioService.getHoldings("PORTFOLIO_B")).thenReturn(Mono.just(Arrays.asList(h2)));
        when(portfolioService.getHoldings("PORTFOLIO_C")).thenReturn(Mono.just(Arrays.asList(h2, h3)));

        Mono<List<String>> resultMono = portfolioController.getPortfoliosByStock("AMZN");

        // Verify results
        StepVerifier.create(resultMono)
                .expectNextMatches(result -> result.size() == 1 && result.contains("PORTFOLIO_A"))
                .verifyComplete();

        Mono<List<String>> resultMono2 = portfolioController.getPortfoliosByStock("GOOGL");

        // Verify results
        StepVerifier.create(resultMono2)
                .expectNextMatches(result -> result.size() == 2 &&
                        result.contains("PORTFOLIO_B") && result.contains("PORTFOLIO_C"))
                .verifyComplete();
    }

    @Test
    public void testGetCashFraction() {
        // Dummy data
        Portfolio p1 = new Portfolio("PORTFOLIO_A", false);
        Portfolio p2 = new Portfolio("PORTFOLIO_B", false);
        List<Portfolio> portfolios = Arrays.asList(p1, p2);

        Cash cash1 = new Cash(100.0);
        Cash cash2 = new Cash(null); // Null cash value

        Holding h1 = new Holding("AMZN", 1000);
        Holding h2 = new Holding("GOOGL", 2000);

        when(portfolioService.getAllPortfolios()).thenReturn(Mono.just(portfolios));
        when(portfolioService.getCash("PORTFOLIO_A")).thenReturn(Mono.just(cash1));
        when(portfolioService.getCash("PORTFOLIO_B")).thenReturn(Mono.just(cash2));

        when(portfolioService.getHoldings("PORTFOLIO_A")).thenReturn(Mono.just(Arrays.asList(h1, h2)));
        when(portfolioService.getHoldings("PORTFOLIO_B")).thenReturn(Mono.just(Arrays.asList(h1)));

        // Expected values
        double totalValueA = 100.0 + 1000 + 2000;
        double fractionA = 100.0 / totalValueA;

        double totalValueB = 0.0 + 1000;
        double fractionB = 0.0; // Cash value is null, treated as 0

        // Call the controller method
        Mono<Map<String, Double>> resultMono = portfolioController.getCashFraction();

        // Verify results
        StepVerifier.create(resultMono)
                .expectNextMatches(result -> {
                    return result.size() == 2 &&
                            result.get("PORTFOLIO_A").equals(fractionA) &&
                            result.get("PORTFOLIO_B").equals(fractionB);
                })
                .verifyComplete();
    }

    @Test
    public void testGetPortfoliosByStockNoMatchingPortfolios() {
        // dummy data
        Portfolio p1 = new Portfolio("PORTFOLIO_A", false);
        List<Portfolio> portfolios = Arrays.asList(p1);

        Holding h1 = new Holding("GOOGL", 2000);

        when(portfolioService.getAllPortfolios()).thenReturn(Mono.just(portfolios));
        when(portfolioService.getHoldings("PORTFOLIO_A")).thenReturn(Mono.just(Arrays.asList(h1)));

        Mono<List<String>> resultMono = portfolioController.getPortfoliosByStock("AMZN");

        StepVerifier.create(resultMono)
                .expectNext(Collections.emptyList())
                .verifyComplete();
    }


    @Test
    public void testGetCashFractionNoPortfolios() {
        when(portfolioService.getAllPortfolios()).thenReturn(Mono.just(Collections.emptyList()));

        Mono<Map<String, Double>> resultMono = portfolioController.getCashFraction();

        // Verify results
        StepVerifier.create(resultMono)
                .expectNext(Collections.emptyMap())
                .verifyComplete();
    }

}
