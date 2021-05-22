package io.github.berrachdis.feignwrapper.util;

import io.github.berrachdis.feignwrapper.model.ResponseWrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonErrorHandlerUtil implements CommonResponseHandler{
    private static final Logger log = LoggerFactory.getLogger(CommonErrorHandlerUtil.class);

    @Override
    public void handleHttpClientError(ResponseWrap response) {
        log.warn("A Client error occurred while calling the client.");
    }

    @Override
    public void handleHttpServerError(ResponseWrap response) {
        log.warn("A Server error occurred while calling the client.");
    }

    @Override
    public void handleSuccessResponse(ResponseWrap response) {
        log.info("The call was successful {}", response.body());
    }
}
