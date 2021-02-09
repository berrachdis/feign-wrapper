package io.github.berrachdis.feignwrapper.core;

import feign.Response;
import io.github.berrachdis.feignwrapper.exception.CustomFeignException;
import io.github.berrachdis.feignwrapper.model.ResponseWrap;
import io.github.berrachdis.feignwrapper.util.Completion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FeignResponseWrapper {
    private static final Logger log = LoggerFactory.getLogger(FeignResponseWrapper.class);
    private final ResponseWrap responseWrap;
    private boolean error;

    private FeignResponseWrapper(Response response) {
        this.responseWrap = new ResponseWrap(response);
        this.error = responseWrap.isError();
    }

    public static FeignResponseWrapper just(Supplier<Response> client) {
        Objects.requireNonNull(client, "Client supplier is null");
        try {
            return resumeOnSuccess(client.get());
        } catch (CustomFeignException ex) {
            return resumeOnError(ex.getResponse());
        }
    }

    public FeignResponseWrapper then(Supplier<Response> clientSupplier) {
        Objects.requireNonNull(clientSupplier, "errorMapper is null");
        if (!isError()) {
            return just(clientSupplier);
        }
        log.error("The previous call has failed {} " + responseWrap);
        return this;
    }

    public static FeignResponseWrapper resumeOnSuccess(Response response) {
        Objects.requireNonNull(response, "Response is null");
        return new FeignResponseWrapper(response);
    }

    public static FeignResponseWrapper resumeOnError(Response response) {
        Objects.requireNonNull(response, "Response is null");
        return new FeignResponseWrapper(response);
    }

    public FeignResponseWrapper doOnError(Consumer<ResponseWrap> errorMapper) {
        Objects.requireNonNull(errorMapper, "errorMapper is null");
        if (isError()) {
            errorMapper.accept(responseWrap);
        }
        return this;
    }

    public FeignResponseWrapper doOnSuccess(Consumer<ResponseWrap> successMapper) {
        Objects.requireNonNull(successMapper, "successMapper is null");
        if (!isError()) {
            successMapper.accept(responseWrap);
        }
        return this;
    }

    public FeignResponseWrapper doOnClientError(Consumer<ResponseWrap> errorMapper) {
        if (responseWrap.series() != null && responseWrap.is4xxClientError()) {
            return doOnError(errorMapper);
        }
        return this;
    }

    public FeignResponseWrapper doOnServerError(Consumer<ResponseWrap> errorMapper) {
        if (responseWrap.series() != null && responseWrap.is5xxServerError()) {
            return doOnError(errorMapper);
        }
        return this;
    }

    public void subscribe(Consumer<ResponseWrap> successMapper, Consumer<ResponseWrap> errorMapper, Completion completion) {
        Objects.requireNonNull(successMapper, "successMapper is null");
        Objects.requireNonNull(errorMapper, "errorMapper is null");
        if (isError()) {
            errorMapper.accept(responseWrap);
        } else {
            successMapper.accept(responseWrap);
        }
        completion.onComplete();
    }

    public boolean isError() {
        return this.error;
    }
}
