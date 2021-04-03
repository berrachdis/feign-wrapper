package io.github.berrachdis.feignwrapper.core;

import feign.Request;
import feign.Response;
import io.github.berrachdis.feignwrapper.model.MockResponseDTO;
import io.github.berrachdis.feignwrapper.util.CommonErrorHandlerUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
class FeignResponseWrapperTest {
    private final Request request = Request.create(Request.HttpMethod.GET, anyString(), new HashMap<>(), "body".getBytes(),
            Charset.defaultCharset(), null);

    @Test
    void justWithNoError() {
        final String body = "{\n" +
                "  \"mock\": \"it's OK\"\n" +
                "}";
        final Supplier<Response> clientSupplier = () ->
                Response.builder().status(HttpStatus.OK.value()).body(body.getBytes()).request(request).build();

        boolean isError = FeignResponseWrapper.just(clientSupplier)
                .doOnClientError(CommonErrorHandlerUtil::handleHttpClientError)
                .doOnServerError(CommonErrorHandlerUtil::handleHttpServerError)
                .isError();
        Assertions.assertFalse(isError);

        final Optional<MockResponseDTO> mockResponseDTO = FeignResponseWrapper.just(clientSupplier)
                .doOnClientError(CommonErrorHandlerUtil::handleHttpClientError)
                .doOnServerError(CommonErrorHandlerUtil::handleHttpServerError)
                .getBodyAsObject(MockResponseDTO.class);
        Assertions.assertTrue(mockResponseDTO.isPresent());
        Assertions.assertEquals("it's OK", mockResponseDTO.get().getMock());
    }

    @Test
    void justWithError() {
        final String body = "{\n" +
                "  \"mock\": \"it's not OK\"\n" +
                "}";
        final Supplier<Response> clientSupplier = () ->
                Response.builder().status(HttpStatus.BAD_REQUEST.value()).body(body.getBytes()).request(request).build();
        FeignResponseWrapper.just(clientSupplier)
                .doOnClientError(response -> CommonErrorHandlerUtil.handleHttpClientError(response))
                .doOnServerError(response -> CommonErrorHandlerUtil.handleHttpServerError(response))
                .subscribe(
                        successResponse -> Assertions.assertNull(successResponse),
                        errorResponse -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.status()),
                        () -> System.out.println("finished")
                );
    }
}