package io.github.berrachdis.feignwrapper.configuration;

import feign.Response;
import feign.Util;
import feign.codec.Decoder;
import io.github.berrachdis.feignwrapper.enumartion.Series;
import io.github.berrachdis.feignwrapper.exception.CustomFeignException;
import io.github.berrachdis.feignwrapper.propertie.FeignWrapperProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public class CustomDecoder
        implements Decoder {
    private static final Logger log = LoggerFactory.getLogger(CustomDecoder.class);
    private final FeignWrapperProperties feignWrapperProperties;
    private final Decoder delegate;

    public CustomDecoder(FeignWrapperProperties feignWrapperProperties, Decoder delegate) {
        Assert.notNull(delegate, "Can't build this decoder with a null delegated decoder");
        this.feignWrapperProperties = feignWrapperProperties;
        this.delegate = delegate;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (!(type instanceof ParameterizedType)) {
            return delegate.decode(response, type);
        }

        if (isParameterizedTypeOf(type, Optional.class)) {
            return decodeOptional(response, type);
        }

        if (isParameterizedTypeOf(type, ResponseEntity.class)) {
            return decodeResponseEntity(response, type);
        }

        return delegate.decode(response, type);
    }

    private boolean isParameterizedTypeOf(Type type, Class<?> clazz) {
        ParameterizedType parameterizedType = (ParameterizedType) type;

        return parameterizedType.getRawType().equals(clazz);
    }

    private Object decodeResponseEntity(Response response, Type type) throws IOException {
        if (feignWrapperProperties.getCustomStatus().contains(response.status()) || Series.valueOf(response.status()) == Series.CLIENT_ERROR || Series.valueOf(response.status()) == Series.SERVER_ERROR) {
            throw new CustomFeignException(response, feignWrapperProperties.getRetry());
        }
        return delegate.decode(response, type);
    }

    private Object decodeOptional(Response response, Type type) throws IOException {
        if (feignWrapperProperties.getCustomStatus().contains(response.status())) {
            log.error("decode optional type of response status {}", response.status());
            return Optional.empty();
        }
        Type enclosedType = Util.resolveLastTypeParameter(type, Optional.class);
        Object decodedValue = delegate.decode(response, enclosedType);
        if (decodedValue == null) {
            return Optional.empty();
        }

        return Optional.of(decodedValue);
    }
}