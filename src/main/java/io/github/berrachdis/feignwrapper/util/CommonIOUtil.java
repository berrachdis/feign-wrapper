package io.github.berrachdis.feignwrapper.util;

import org.slf4j.Logger;

import feign.Response;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class CommonIOUtil {
    private static final Logger log = LoggerFactory.getLogger(CommonIOUtil.class);

    private CommonIOUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String getResponseBody(Response response) {
        String body = null;
        if (response != null && response.body() != null) {
            try {
                body = IOUtils.toString(response.body().asInputStream());
            } catch (IOException e) {
                log.error("Failed to process error response body", e);
            }
        } else {
            log.error("Response body is null with status : [{}]", response.status());
        }
        return body;
    }
}
