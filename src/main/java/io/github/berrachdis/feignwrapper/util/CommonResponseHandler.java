package io.github.berrachdis.feignwrapper.util;

import io.github.berrachdis.feignwrapper.model.ResponseWrap;

public interface CommonResponseHandler {
    void handleSuccessResponse(ResponseWrap responseWrap);
    void handleHttpClientError(ResponseWrap responseWrap);
    void handleHttpServerError(ResponseWrap responseWrap);
}
