package io.github.berrachdis.feignwrapper.core;

import feign.Response;
import io.github.berrachdis.feignwrapper.model.ResponseWrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Supplier;

public class FeignResponseWrapper {
    private static final Logger log = LoggerFactory.getLogger(FeignResponseWrapper.class);
    private final ResponseWrap responseWrap;
    private boolean error;

    public FeignResponseWrapper(Response response) {
        this.responseWrap = new ResponseWrap(response);
        this.error = responseWrap.isError();
    }

    public FeignResponseWrapper just(Supplier<Response> client) {
        Objects.requireNonNull(client, "Client supplier is null");
        // TODO : Add error management in this part
        return resumeOnSuccess(client.get());
    }

    public FeignResponseWrapper resumeOnSuccess(Response response) {
        Objects.requireNonNull(response, "Response is null");
        return new FeignResponseWrapper(response);
    }

    public FeignResponseWrapper resumeOnError(Response response ){
        Objects.requireNonNull(response, "Response is null");
        return new FeignResponseWrapper(response);
    }

    public boolean isError() {
        return this.error;
    }
}
