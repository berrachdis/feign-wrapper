package io.github.berrachdis.feignwrapper.util;

import io.github.berrachdis.feignwrapper.model.ResponseWrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommonErrorHandlerUtil {
    private static final Logger log = LoggerFactory.getLogger(CommonErrorHandlerUtil.class);

    private CommonErrorHandlerUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void handleHttpClientError(ResponseWrap response) {
        log.warn("A Client error occurred while calling the client.");
    }

    public static void handleHttpServerError(ResponseWrap response) {
        log.warn("A Server error occurred while calling the client.");
    }
}
