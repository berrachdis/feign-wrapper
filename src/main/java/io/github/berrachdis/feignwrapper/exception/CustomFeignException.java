package io.github.berrachdis.feignwrapper.exception;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import io.github.berrachdis.feignwrapper.enumartion.Series;
import io.github.berrachdis.feignwrapper.propertie.FeignWrapperProperties;
import io.github.berrachdis.feignwrapper.util.CommonIOUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Date;

public class CustomFeignException extends FeignException {
    private final transient ResponseEntity<String> responseEntity;

    public  CustomFeignException(Response response, FeignWrapperProperties.Retry retry) {
        super(response.status(), response.reason(), response.request());
        this.responseEntity = mapToResponseEntity(response, retry);
    }

    public ResponseEntity<String> getResponse() {
        return responseEntity;
    }

    private ResponseEntity<String> mapToResponseEntity(Response response, FeignWrapperProperties.Retry retry) {
        final String responseBody = CommonIOUtil.getResponseBody(response);
        if ((!CollectionUtils.isEmpty(retry.getSeriesSet()) && retry.getSeriesSet().contains(Series.valueOf(response.status()))) || (!CollectionUtils.isEmpty(retry.getRetryableStatusCodes()) && retry.getRetryableStatusCodes().contains(response.status()))) {
            throw new RetryableException(response.status(), responseBody, response.request().httpMethod(), Date.from(Instant.now()), response.request());
        }
        return ResponseEntity.status(response.status()).body(responseBody);
    }
}
