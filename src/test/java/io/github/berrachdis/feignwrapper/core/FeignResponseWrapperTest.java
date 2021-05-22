package io.github.berrachdis.feignwrapper.core;

import feign.Request;
import io.github.berrachdis.feignwrapper.model.MockResponseDTO;
import io.github.berrachdis.feignwrapper.util.CommonErrorHandlerUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
class FeignResponseWrapperTest {
    private final CommonErrorHandlerUtil commonErrorHandlerUtil = new CommonErrorHandlerUtil();
    private final Request request = Request.create(Request.HttpMethod.GET, anyString(), new HashMap<>(), "body".getBytes(),
            Charset.defaultCharset(), null);

    @Test
    void justWithNoError() {
        final String body = "{\n" +
                "  \"mock\": \"it's OK\"\n" +
                "}";
        final Supplier<ResponseEntity<String>> clientSupplier = () -> ResponseEntity.status(299).body(body);

        boolean isError = FeignResponseWrapper.just(clientSupplier)
                .doOnClientError(commonErrorHandlerUtil::handleHttpClientError)
                .doOnServerError(commonErrorHandlerUtil::handleHttpServerError)
                .doOnSuccess(commonErrorHandlerUtil::handleSuccessResponse)
                .isError();
        Assertions.assertFalse(isError);

        final Optional<MockResponseDTO> mockResponseDTO = FeignResponseWrapper.just(clientSupplier)
                .doOnClientError(commonErrorHandlerUtil::handleHttpClientError)
                .doOnServerError(commonErrorHandlerUtil::handleHttpServerError)
                .getBodyAsObject(MockResponseDTO.class);
        Assertions.assertTrue(mockResponseDTO.isPresent());
        Assertions.assertEquals("it's OK", mockResponseDTO.get().getMock());
    }

    @Test
    void justWithError() {
        final String body = "{\n" +
                "  \"mock\": \"it's not OK\"\n" +
                "}";
        final Supplier<ResponseEntity<String>> clientSupplier = () -> ResponseEntity.badRequest().body(body);
        FeignResponseWrapper.just(clientSupplier)
                .doOnClientError(commonErrorHandlerUtil::handleHttpClientError)
                .doOnServerError(commonErrorHandlerUtil::handleHttpServerError)
                .subscribe(
                        successResponse -> Assertions.assertNull(successResponse),
                        errorResponse -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.status()),
                        () -> System.out.println("finished")
                );
    }
}