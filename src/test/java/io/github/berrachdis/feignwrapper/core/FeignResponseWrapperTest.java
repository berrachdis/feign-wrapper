package io.github.berrachdis.feignwrapper.core;

import feign.Request;
import io.github.berrachdis.feignwrapper.exception.ConversionException;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
class FeignResponseWrapperTest {
    private final CommonErrorHandlerUtil commonErrorHandlerUtil = new CommonErrorHandlerUtil();
    private final Request request = Request.create(Request.HttpMethod.GET, anyString(), new HashMap<>(), "body".getBytes(),
            Charset.defaultCharset(), null);

    @Test
    void justWithSuccessResponse() {
        final String body = "{\n" +
                "  \"mock\": \"it's OK\"\n" +
                "}";
        final Supplier<ResponseEntity<String>> clientSupplier = () -> ResponseEntity.status(299).body(body);
        FeignResponseWrapper.just(clientSupplier)
                .doOnSuccess(commonErrorHandlerUtil::handleSuccessResponse)
                .doOnClientError(commonErrorHandlerUtil::handleHttpClientError)
                .doOnServerError(commonErrorHandlerUtil::handleHttpServerError)
                .subscribe(
                        successResponse -> {
                            assertFalse(successResponse.isError());
                            assertTrue("No matching constant for 299".equals(successResponse.reason()));
                            assertTrue(successResponse.body().contains("it's OK"));
                        },
                        errorResponse -> assertNull(errorResponse),
                        () -> System.out.println("finished")
                );
    }

    @Test
    void justWithBadErrorResponse() {
        final String body = "bad request";
        final Supplier<ResponseEntity<String>> clientSupplier = () -> ResponseEntity.status(400).body(body);

        FeignResponseWrapper.just(clientSupplier)
                .doOnSuccess(commonErrorHandlerUtil::handleSuccessResponse)
                .doOnClientError(commonErrorHandlerUtil::handleHttpClientError)
                .doOnServerError(commonErrorHandlerUtil::handleHttpServerError)
                .subscribe(
                        successResponse -> assertNull(successResponse),
                        errorResponse -> {
                            assertTrue(errorResponse.isError());
                            assertTrue(HttpStatus.BAD_REQUEST.getReasonPhrase().equals(errorResponse.reason()));
                            assertTrue(errorResponse.body().contains("bad request"));
                        },
                        () -> System.out.println("finished")
                );
    }

    @Test
    void justWithServerErrorResponse() {
        final String body = "Internal error";
        final Supplier<ResponseEntity<String>> clientSupplier = () -> ResponseEntity.status(500).body(body);

        FeignResponseWrapper.just(clientSupplier)
                .doOnSuccess(commonErrorHandlerUtil::handleSuccessResponse)
                .doOnClientError(commonErrorHandlerUtil::handleHttpClientError)
                .doOnServerError(commonErrorHandlerUtil::handleHttpServerError)
                .subscribe(
                        successResponse -> assertNull(successResponse),
                        errorResponse -> {
                            assertTrue(errorResponse.isError());
                            assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase().equals(errorResponse.reason()));
                            assertTrue(errorResponse.body().contains("Internal error"));
                        },
                        () -> System.out.println("finished")
                );
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
                        successResponse -> assertNull(successResponse),
                        errorResponse -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.status()),
                        () -> System.out.println("finished")
                );
    }

    @Test
    void testGetBodyAsObjectWithGoodJson() {
        final String body = "{\n" +
                "  \"mock\": \"it's OK\"\n" +
                "}";
        final Supplier<ResponseEntity<String>> clientSupplier = () -> ResponseEntity.badRequest().body(body);
        FeignResponseWrapper.just(clientSupplier).getBodyAsObject(MockResponseDTO.class)
                .ifPresent(mockResponseDTO -> Assertions.assertEquals("it's OK", mockResponseDTO.getMock()));
    }

    @Test
    void testGetBodyAsObjectWithEmptyBody() {
        final String emptyBody = null;
        final Supplier<ResponseEntity<String>> clientSupplier = () -> ResponseEntity.badRequest().body(emptyBody);
        Optional<MockResponseDTO> responseDTOOptional = FeignResponseWrapper.just(clientSupplier).getBodyAsObject(MockResponseDTO.class);
        assertFalse(responseDTOOptional.isPresent());
    }

    @Test
    void testGetBodyAsObjectWithBadJson() {
        final String body = "{\"mock\": \"it's a bad JSON\"";
        final Supplier<ResponseEntity<String>> clientSupplier = () -> ResponseEntity.badRequest().body(body);
        assertThrows(ConversionException.class, () -> FeignResponseWrapper.just(clientSupplier).getBodyAsObject(MockResponseDTO.class));
    }

    @Test
    void testGetBodyAsListOfObjectsWithGoodJson() {
        final String body = "[\n" + "\t{\"mock\": \"it's OK\"}\n" + "]";
        final Supplier<ResponseEntity<String>> clientSupplier = () -> ResponseEntity.badRequest().body(body);
        FeignResponseWrapper.just(clientSupplier).getBodyAsListOfObjects(MockResponseDTO.class)
                .ifPresent(mockResponseDTOList -> Assertions.assertEquals("it's OK", mockResponseDTOList.get(0).getMock()));
    }

    @Test
    void testGetBodyAsListOfObjectsWithEmptyJson() {
        final String emptyBody = null;
        final Supplier<ResponseEntity<String>> clientSupplier = () -> ResponseEntity.badRequest().body(emptyBody);
        Optional<List<MockResponseDTO>> responseDTOOptional = FeignResponseWrapper.just(clientSupplier).getBodyAsListOfObjects(MockResponseDTO.class);
        assertFalse(responseDTOOptional.isPresent());
    }

    @Test
    void testGetBodyAsListOfObjectsWithBadJson() {
        final String body = "[\n" + "\t{\"mock\": \"it's OK\"\n" + "]";
        final Supplier<ResponseEntity<String>> clientSupplier = () -> ResponseEntity.badRequest().body(body);
        assertThrows(ConversionException.class, () -> FeignResponseWrapper.just(clientSupplier).getBodyAsListOfObjects(MockResponseDTO.class));
    }
}