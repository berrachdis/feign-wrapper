package com.example.feignhelper.configuration;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Slf4j
public class CustomRetryer implements Retryer {
    private int retryMaxAttempt;

    private long retryInterval;

    private int attempt = 1;


    public CustomRetryer(int retryMaxAttempt, Long retryInterval) {
        this.retryMaxAttempt = retryMaxAttempt;
        this.retryInterval = retryInterval;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        log.info("Feign retry attempt {} due to {} ", attempt, e.getMessage());

        handleRestClientError(e);

        if(attempt++ == retryMaxAttempt){
            throw e;
        }
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

    }

    @Override
    public Retryer clone() {
        return new CustomRetryer(6, 2000L);
    }

    public static void handleRestClientError(Throwable ex) {
        if (ex.getCause() != null) {
            if (ex.getCause() instanceof SocketTimeoutException) {
                // Handle socket timeout
                log.warn("A socket time out error occurred while calling the client [{}].", ex.getMessage());
                return;
            } else if (ex.getCause() instanceof SSLHandshakeException) {
                // Handle Sock handshake
                log.warn("A socket handshake error occurred while calling the client [{}].", ex.getMessage());
                return;
            } else if (ex.getCause() instanceof ConnectException) {
                // Handle Connection exception
                log.warn("A connection exception error occurred while calling the client [{}].", ex.getMessage());
                return;
            }
        }
        log.warn("Unknown error occurred while calling the client [{}].", ex.getMessage());
    }
}
