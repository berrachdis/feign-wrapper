package io.github.berrachdis.feignwrapper.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import io.github.berrachdis.feignwrapper.exception.ConversionException;
import io.github.berrachdis.feignwrapper.exception.CustomFeignException;
import io.github.berrachdis.feignwrapper.model.ResponseWrap;
import io.github.berrachdis.feignwrapper.util.Completion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FeignResponseWrapper {
    private static final String ERROR_MAPPER_IS_NULL = "errorMapper is null";
    private static final String DESERIALIZATION_ERROR = "Could not deserialize to the object due to [%s]";
    private static final Logger LOGGER = LoggerFactory.getLogger(FeignResponseWrapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResponseWrap responseWrap;
    private final boolean isError;

    private FeignResponseWrapper(ResponseEntity<String> response) {
        this.responseWrap = new ResponseWrap(response);
        this.isError = responseWrap.isError();
    }

    /**
     * Method used to do the call and return instance of {@FeignResponseWrapper} which contains the result of the call
     */
    public static FeignResponseWrapper just(Supplier<ResponseEntity<String>> feignClient) {
        Objects.requireNonNull(feignClient, "Client supplier is null");
        try {
            return resume(feignClient.get());
        } catch (CustomFeignException ex) {
            return resume(ex.getResponse());
        } catch (RetryableException ex) {
            return resume(ResponseEntity.status(ex.status()).body(ex.getMessage()));
        }
    }

    /**
     * Method used to do another call if the current call succeeded
     *
     * @param clientSupplier supplier used to do the new call
     * @return new instance of {@literal FeignResponseWrapper} contains the result of the last call if the previous call was successful,
     * otherwise the current object which contains the error response
     */
    public FeignResponseWrapper then(Supplier<ResponseEntity<String>> clientSupplier) {
        Objects.requireNonNull(clientSupplier, ERROR_MAPPER_IS_NULL);
        if (!isError()) {
            return just(clientSupplier);
        }
        LOGGER.error("The previous call has failed {} ", responseWrap);
        return this;
    }

    /**
     * Method used to convert instance of {@literal Response} into {@literal FeignResponseWrapper}
     *
     * @param response the result of the call
     * @return instance of {@literal FeignResponseWrapper}
     */
    public static FeignResponseWrapper resume(ResponseEntity<String> response) {
        Objects.requireNonNull(response, "Response is null");
        return new FeignResponseWrapper(response);
    }

    /**
     * Method used to process the error response in case of client or server error
     *
     * @param errorMapper instance of {@literal Consumer<ResponseWrap>} to apply on the response
     * @return the instance of the current object
     */
    public FeignResponseWrapper doOnError(Consumer<ResponseWrap> errorMapper) {
        Objects.requireNonNull(errorMapper, ERROR_MAPPER_IS_NULL);
        if (isError()) {
            errorMapper.accept(responseWrap);
        }
        return this;
    }

    /**
     * Method used to process the success response
     *
     * @param successMapper instance of {@literal Consumer<ResponseWrap>} to apply on the response
     * @return the instance of the current object
     */
    public FeignResponseWrapper doOnSuccess(Consumer<ResponseWrap> successMapper) {
        Objects.requireNonNull(successMapper, "successMapper is null");
        if (!isError()) {
            successMapper.accept(responseWrap);
        }
        return this;
    }

    /**
     * Method used to process the response in case of client error
     *
     * @param errorMapper instance of {@literal Consumer<ResponseWrap>} to apply on the response
     * @return the instance of the current object
     */
    public FeignResponseWrapper doOnClientError(Consumer<ResponseWrap> errorMapper) {
        if (responseWrap.series() != null && responseWrap.is4xxClientError()) {
            return doOnError(errorMapper);
        }
        return this;
    }

    /**
     * Method used to process the response in case internal server error
     *
     * @param errorMapper instance of {@literal Consumer<ResponseWrap>} to apply on the response
     * @return the instance of the current object
     */
    public FeignResponseWrapper doOnServerError(Consumer<ResponseWrap> errorMapper) {
        if (responseWrap.series() != null && responseWrap.is5xxServerError()) {
            return doOnError(errorMapper);
        }
        return this;
    }

    /**
     * Method used to read the result of the call
     *
     * @param successMapper instance of {@literal Consumer<ResponseWrap>} applied on the success result when there is no error
     * @param errorMapper   instant of {@literal Consumer<ResponseWrap>} applied on the error result when there is an error
     * @param completion    instant of {@literal Completion} applied at the end
     */
    public void subscribe(Consumer<ResponseWrap> successMapper, Consumer<ResponseWrap> errorMapper, Completion completion) {
        Objects.requireNonNull(successMapper, "successMapper is null");
        Objects.requireNonNull(errorMapper, ERROR_MAPPER_IS_NULL);
        if (isError()) {
            errorMapper.accept(responseWrap);
        } else {
            successMapper.accept(responseWrap);
        }
        completion.onComplete();
    }

    /**
     * Method used to deserialize JSON content from given response content
     *
     * @return instance of {@literal Optional} which contains the result of deserialization
     * expected for result type (or has other mismatch issues or contains invalid input)
     */
    public <T> Optional<T> getBodyAsObject(Class<T> toValueType) {
        if (!StringUtils.hasLength(responseWrap.body())) {
            LOGGER.warn("The response body is null or empty");
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(objectMapper.readValue(this.responseWrap.body(), toValueType));
        } catch (JsonProcessingException e) {
            throw new ConversionException(String.format(DESERIALIZATION_ERROR, e.getMessage()));
        }
    }

    public <T> Optional<List<T>> getBodyAsListOfObjects(Class<T> toValueType) {
        if (!StringUtils.hasLength(responseWrap.body())) {
            LOGGER.warn("The response body is null or empty");
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(objectMapper.readValue(this.responseWrap.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, toValueType)));
        } catch (JsonProcessingException e) {
            throw new ConversionException(String.format(DESERIALIZATION_ERROR, e.getMessage()));
        }
    }

    public boolean isError() {
        return this.isError;
    }
}
