package org.example.portfolio.service;

public class PortfolioServiceException extends Exception
{
    public PortfolioServiceException(String message)
    {
        super(message);
    }

    public PortfolioServiceException(String message, Throwable throwable)
    {
        super(message, throwable);
    }
}
