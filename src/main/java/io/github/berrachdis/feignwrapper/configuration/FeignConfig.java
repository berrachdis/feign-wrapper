package io.github.berrachdis.feignwrapper.configuration;

import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import io.github.berrachdis.feignwrapper.exception.CustomFeignException;
import io.github.berrachdis.feignwrapper.propertie.FeignWrapperProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel(@Value("${feign.client.config.default.loggerLevel:NONE}") final String level) {
        return feign.Logger.Level.valueOf(level);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> new CustomFeignException(response);
    }

    @Bean
    Retryer retryer(FeignWrapperProperties feignWrapperProperties) {
        return new CustomRetryer(feignWrapperProperties.getRetry().getMaxAttempt(),
                feignWrapperProperties.getRetry().getInterval());
    }
}