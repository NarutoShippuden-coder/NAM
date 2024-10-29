package org.example.portfolio.service;

import org.example.portfolio.model.Cash;
import org.example.portfolio.model.Holding;
import org.example.portfolio.model.Portfolio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Service class that fetches portfolio data from a remote API.
 * This class can be used to fetch list of portfolios, their holdings and cash amount.
 */
@Service
public class PortfolioService {

    @Value("${server.base.uri}")   /* to be able to mock test*/
    private String SERVER_API_URL;
    private final static String ENDPOINT_PORTFOLIOS = "/portfolios";
    private final static String ENDPOINT_HOLDINGS = "/holdings";
    private final static String ENDPOINT_CASH = "/cash";
    private final static String BACKSLASH = "/";
    private int TIMEOUT_IN_SECONDS = 8;

    @Autowired
    private WebClient.Builder webclientBuilder;

    /**
     * Constructs a PortfolioService with a given WebClient.Builder.
     *
     * @param webclientBuilder a builder for creating WebClient instances
     */
    public PortfolioService(WebClient.Builder webclientBuilder) {
        this.webclientBuilder = webclientBuilder;
    }

    /**
     * Retrieves all portfolios from the remote API.
     *
     * @return a Mono containing a list of all portfolios
     * @throws PortfolioServiceException if a 4xx error occurs while retrieving portfolios
     */
    public Mono<List<Portfolio>> getAllPortfolios() {
        return
                webclientBuilder.build()
                        .get()
                        .uri(SERVER_API_URL + ENDPOINT_PORTFOLIOS)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError,
                                error -> Mono.error(new PortfolioServiceException("Error while retrieving portfolios.")))
                        .bodyToMono(new ParameterizedTypeReference<List<Portfolio>>() {
                        })
                        .timeout(Duration.ofSeconds(TIMEOUT_IN_SECONDS));
    }

    /**
     * Retrieves holdings for a specified portfolio.
     *
     * @param portfolioName the name of the portfolio for which holdings are to be retrieved
     * @return a Mono containing a list of holdings for the specified portfolio
     * @throws PortfolioServiceException if a 4xx error occurs while retrieving holdings
     */
    public Mono<List<Holding>> getHoldings(String portfolioName) {
        return webclientBuilder.build()
                .get()
                .uri(SERVER_API_URL + BACKSLASH + portfolioName + ENDPOINT_HOLDINGS)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        error -> Mono.error(new PortfolioServiceException("Error while retrieving Holding for " + portfolioName)))
                .bodyToMono(new ParameterizedTypeReference<List<Holding>>() {
                })
               .timeout(Duration.ofSeconds(TIMEOUT_IN_SECONDS));
    }

    /**
     * Retrieves cash information for a specified portfolio.
     *
     * @param portfolioName the name of the portfolio for which cash is to be retrieved
     * @return a Mono containing the cash information for the specified portfolio
     * @throws PortfolioServiceException if a 4xx error occurs while retrieving cash
     */
    public Mono<Cash> getCash(String portfolioName) {
        return webclientBuilder.build()
                .get()
                .uri(SERVER_API_URL + BACKSLASH + portfolioName + ENDPOINT_CASH)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        error -> Mono.error(new PortfolioServiceException("Error while retrieving cash for " + portfolioName)))
                .bodyToMono(Cash.class)
                .timeout(Duration.ofSeconds(TIMEOUT_IN_SECONDS));
    }


}
