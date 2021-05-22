package io.github.berrachdis.feignwrapper.configuration;

import feign.Logger;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import io.github.berrachdis.feignwrapper.exception.CustomFeignException;
import io.github.berrachdis.feignwrapper.propertie.FeignWrapperProperties;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel(@Value("${feign.client.config.default.loggerLevel:NONE}") final String level) {
        return feign.Logger.Level.valueOf(level);
    }

    @Bean
    public ErrorDecoder errorDecoder(FeignWrapperProperties feignWrapperProperties) {
        return (methodKey, response) -> new CustomFeignException(response, feignWrapperProperties.getRetry());
    }

    @Bean
    public Decoder notFoundAwareDecoder(ObjectFactory<HttpMessageConverters> messageConverters, FeignWrapperProperties feignWrapperProperties) {
        return new CustomDecoder(feignWrapperProperties, new ResponseEntityDecoder(new SpringDecoder(messageConverters)));
    }

    @Bean
    public Retryer retryer(FeignWrapperProperties feignWrapperProperties) {
        return new CustomRetryer(feignWrapperProperties);
    }
}