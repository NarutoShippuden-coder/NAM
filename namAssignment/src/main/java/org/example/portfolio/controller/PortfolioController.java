package org.example.portfolio.controller;

import org.example.portfolio.model.Cash;
import org.example.portfolio.model.Holding;
import org.example.portfolio.model.Portfolio;
import org.example.portfolio.service.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


/**
 * Controller class for handling requests related to portfolios.
 * This class provides endpoints to retrieve
 *  1. List of portfolios that contain a given stock
 *  2. Cash as a fraction of full portfolio value for all portfolios as a single call
 */
@RestController
public class PortfolioController {

    private static final Logger log = LoggerFactory.getLogger(PortfolioController.class);
    @Autowired
    private PortfolioService portfolioService;



    @GetMapping("/portfolios/stock/{stockId}")
    @Operation(summary = "Get portfolio Names containing a specific stock",
            description = "Returns list of portfolios containing stock")
    public Mono<List<String>> getPortfoliosByStock(
            @Parameter(description = "Stock symbol to search for", required = true)
            @PathVariable String stockId) {
        return portfolioService.getAllPortfolios()
                .doOnSuccess(portfolios -> log.info("Retrieved {} portfolios", portfolios.size()))
                .onErrorResume(error -> {
                    log.error("Timeout or error occurred while retrieving portfolios, returning an empty list.");
                    return Mono.just(Collections.emptyList());
                })
                .flatMapMany(Flux::fromIterable)
                .flatMap(p -> portfolioService.getHoldings(p.getName())
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMapMany(Flux::fromIterable)
                        .filter(h -> h.getStockId().equalsIgnoreCase(stockId))
                        .take(1) //stop further processing as soon as a match is found
                        .map(b -> p.getName()))
                .collectList()
                .doOnError(error -> log.error("Error processing request for stockId: {}", stockId, error))
                .onErrorReturn(Collections.emptyList());
    }

    @GetMapping("/portfolios/portfolio-cash-fraction")
    @Operation(summary = "Get cash as a fraction of full portfolio value for all portfolios",
            description = "Returns portfolio and its cash as a fraction of full portfolio value")
    public Mono<Map<String, Double>> getCashFraction() {
        return portfolioService.getAllPortfolios()
                .doOnSuccess(portfolios -> log.info("Retrieved {} portfolios for cash fraction calculation ", portfolios.size()))
                .onErrorResume(error -> {
                    log.error("Timeout or error occurred while retrieving portfolios, returning an empty list.");
                    return Mono.just(Collections.emptyList());
                })
                .flatMapMany(Flux::fromIterable)
                .flatMap(p -> {
                    return calculateCashFraction(p)
                            .onErrorResume( e-> {
                                log.error("Error processing cash-fraction for portfolio:" + p.getName(), e);
                                Map<String,Double> result = new HashMap<>();
                                result.put(p.getName(), 0.0);
                                return Mono.just(result);
                            });
                })
                .reduce(new HashMap<>(), (map1, map2) -> {
                    map1.putAll(map2);
                    return map1; //combine the result into a single map
                });
    }

    private Mono<Map<String, Double>> calculateCashFraction(Portfolio p) {
        Mono<Cash> cashMono = portfolioService.getCash(p.getName())
                .subscribeOn(Schedulers.boundedElastic());

        return cashMono.flatMap(cash -> {
            double cashValue = cash.getValue() != null ? cash.getValue() : 0.0;

            // Only fetch holdings if cashValue is greater than zero to reduce api call time
            if (cashValue > 0) {
                Mono<List<Holding>> holdingsMono = portfolioService.getHoldings(p.getName())
                        .subscribeOn(Schedulers.boundedElastic());

                return holdingsMono.map(holdings -> {
                    double holdingsValue = holdings.stream()
                            .mapToDouble(Holding::getValue)
                            .sum();
                    double totalValue = cashValue + holdingsValue;
                    double fraction = totalValue > 0 ? cashValue / totalValue : 0.0;

                    Map<String, Double> result = new HashMap<>();
                    result.put(p.getName(), fraction);
                    return result;
                });
            } else {
                // If there's no cash, return default value
                Map<String, Double> result = new HashMap<>();
                result.put(p.getName(), 0.0);
                return Mono.just(result);
            }
        });
    }


}
