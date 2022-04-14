package io.github.berrachdis.feignwrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import feign.Feign;
import feign.Logger;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import io.github.berrachdis.feignwrapper.client.GitHub;
import io.github.berrachdis.feignwrapper.configuration.CustomDecoder;
import io.github.berrachdis.feignwrapper.configuration.CustomRetryer;
import io.github.berrachdis.feignwrapper.configuration.FeignConfig;
import io.github.berrachdis.feignwrapper.core.FeignResponseWrapper;
import io.github.berrachdis.feignwrapper.model.Contributor;
import io.github.berrachdis.feignwrapper.propertie.FeignWrapperProperties;
import io.github.berrachdis.feignwrapper.util.CommonErrorHandlerUtil;

@SpringBootTest
@EnableAutoConfiguration
@ContextConfiguration(classes = { FeignConfig.class, CustomRetryer.class, CustomDecoder.class, FeignWrapperProperties.class })
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class FeignWrapperApplicationTests {
	@MockBean
	private CommonErrorHandlerUtil commonErrorHandlerUtil;
	@Autowired
	private Logger.Level feignLoggerLevel;
	@Autowired
	private ErrorDecoder errorDecoder;
	@Autowired
	private Decoder notFoundAwareDecoder;
	@Autowired
	private Retryer retryer;

	private GitHub github;

	@BeforeEach
	void setUp() {
		github = Feign.builder()
				.logger(new Logger.ErrorLogger())
				.logLevel(feignLoggerLevel)
				.decoder(notFoundAwareDecoder)
				.errorDecoder(errorDecoder)
				.retryer(retryer)
				.target(GitHub.class, "https://api.github.com");
	}

	@Test
	void testWithSuccessResponse() {
		System.out.println("Let's fetch and check if the contributor berrachdis exist.");
		FeignResponseWrapper.just(() -> github.contributors("berrachdis", "feign-wrapper"))
				.doOnSuccess(commonErrorHandlerUtil::handleSuccessResponse)
				.doOnClientError(commonErrorHandlerUtil::handleHttpClientError)
				.doOnServerError(commonErrorHandlerUtil::handleHttpServerError)
				.getBodyAsListOfObjects(Contributor.class)
				.ifPresent(contributors ->
						assertTrue(contributors.stream().filter(contributor -> "berrachdis".equals(contributor.getLogin())).findFirst().isPresent()));
		verify(commonErrorHandlerUtil, times(1)).handleSuccessResponse(any());
		verify(commonErrorHandlerUtil, times(0)).handleHttpClientError(any());
		verify(commonErrorHandlerUtil, times(0)).handleHttpServerError(any());
	}

	@Test
	void testWithUnauthorizedErrorResponse() {
		System.out.println("Let's request projects API without auth credentials.");
		FeignResponseWrapper.just(() -> github.projects(1))
				.doOnSuccess(commonErrorHandlerUtil::handleSuccessResponse)
				.doOnClientError(commonErrorHandlerUtil::handleHttpClientError)
				.doOnServerError(commonErrorHandlerUtil::handleHttpServerError)
				.subscribe(
						successResponse -> assertNull(successResponse),
						errorResponse -> assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.status()),
						() -> System.out.println("## onComplete ##")
				);
		verify(commonErrorHandlerUtil, times(0)).handleSuccessResponse(any());
		verify(commonErrorHandlerUtil, times(1)).handleHttpClientError(any());
		verify(commonErrorHandlerUtil, times(0)).handleHttpServerError(any());
	}

	@Test
	void testWithNotFoundErrorResponse() {
		System.out.println("Let's request an undefined API.");
		FeignResponseWrapper.just(() -> github.projects(null))
				.doOnSuccess(commonErrorHandlerUtil::handleSuccessResponse)
				.doOnClientError(commonErrorHandlerUtil::handleHttpClientError)
				.doOnServerError(commonErrorHandlerUtil::handleHttpServerError)
				.subscribe(
						successResponse -> assertNull(successResponse),
						errorResponse -> assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.status()),
						() -> System.out.println("## onComplete ##")
				);
		verify(commonErrorHandlerUtil, times(0)).handleSuccessResponse(any());
		verify(commonErrorHandlerUtil, times(1)).handleHttpClientError(any());
		verify(commonErrorHandlerUtil, times(0)).handleHttpServerError(any());
	}

	@Test
	void testWithSuccessResponseThenNotFoundErrorResponse() {
		System.out.println("Let's fetch and check if the contributor berrachdis exist then request the undefined API.");
		FeignResponseWrapper.just(() -> github.contributors("berrachdis", "feign-wrapper"))
				.doOnSuccess(commonErrorHandlerUtil::handleSuccessResponse)
				.doOnClientError(commonErrorHandlerUtil::handleHttpClientError)
				.doOnServerError(commonErrorHandlerUtil::handleHttpServerError)
				.then(() -> github.projects(null))
				.subscribe(
						successResponse -> assertNull(successResponse),
						errorResponse -> assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.status()),
						() -> System.out.println("## onComplete ##")
				);
		verify(commonErrorHandlerUtil, times(1)).handleSuccessResponse(any());
		verify(commonErrorHandlerUtil, times(0)).handleHttpClientError(any());
		verify(commonErrorHandlerUtil, times(0)).handleHttpServerError(any());
	}

	@Test
	void testWithUnauthorizedErrorResponseThenSuccessResponse() {
		System.out.println("Let's request projects API without auth credentials then Let's fetch and check if the contributor berrachdis exist.");
		FeignResponseWrapper.just(() -> github.projects(1))
				.doOnSuccess(commonErrorHandlerUtil::handleSuccessResponse)
				.doOnClientError(commonErrorHandlerUtil::handleHttpClientError)
				.doOnServerError(commonErrorHandlerUtil::handleHttpServerError)
				.then(() -> github.contributors("berrachdis", "feign-wrapper"))
				.subscribe(
						successResponse -> assertNull(successResponse),
						errorResponse -> assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.status()),
						() -> System.out.println("## onComplete ##")
				);
		verify(commonErrorHandlerUtil, times(0)).handleSuccessResponse(any());
		verify(commonErrorHandlerUtil, times(1)).handleHttpClientError(any());
		verify(commonErrorHandlerUtil, times(0)).handleHttpServerError(any());
	}
}
