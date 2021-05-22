package io.github.berrachdis.feignwrapper.configuration;

import feign.RetryableException;
import feign.Retryer;
import io.github.berrachdis.feignwrapper.enumartion.Series;
import io.github.berrachdis.feignwrapper.propertie.FeignWrapperProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

public class CustomRetryer implements Retryer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomRetryer.class);
    private final FeignWrapperProperties feignWrapperProperties;
    private int attempt = 1;

    public CustomRetryer(FeignWrapperProperties feignWrapperProperties) {
        this.feignWrapperProperties = feignWrapperProperties;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        LOGGER.warn("Feign retry attempt {} due to  HttpStatus = {}, Series = {}", attempt, e.status(), Series.valueOf(e.status()).message());

        handleRestClientError(e);

        if(attempt++ == feignWrapperProperties.getRetry().getMaxAttempt()){
            throw e;
        }
        try {
            Thread.sleep(feignWrapperProperties.getRetry().getInterval());
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

    }

    @Override
    public Retryer clone() {
        return new CustomRetryer(feignWrapperProperties);
    }

    private static void handleRestClientError(Throwable ex) {
        if (ex.getCause() != null) {
            if (ex.getCause() instanceof SocketTimeoutException) {
                // Handle socket timeout
                LOGGER.warn("A socket time out error occurred while calling the client [{}].", ex.getMessage());
                return;
            } else if (ex.getCause() instanceof SSLHandshakeException) {
                // Handle Sock handshake
                LOGGER.warn("A socket handshake error occurred while calling the client [{}].", ex.getMessage());
                return;
            } else if (ex.getCause() instanceof ConnectException) {
                // Handle Connection exception
                LOGGER.warn("A connection exception error occurred while calling the client [{}].", ex.getMessage());
                return;
            }
        }
    }
}
