package io.github.berrachdis.feignwrapper.exception;

import feign.FeignException;
import feign.Response;

public class CustomFeignException extends FeignException {
    private final transient Response response;

    public CustomFeignException(Response response) {
        super(response.status(), response.reason(), response.request());
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }
}
