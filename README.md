[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=berrachdis_feign-wrapper&metric=coverage)](https://sonarcloud.io/summary/new_code?id=berrachdis_feign-wrapper)

# feign-wrapper
Feign-wrapper tool to use the openfeign library easily in functional way

## Get it as a maven dependency  :

```XML
   <dependency>
        <groupId>io.github.berrachdis</groupId>
        <artifactId>feign-wrapper</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
```

Usage:
## 1. Configuration
In addition to the basic configurations provided by the library ``feign-client``, the ``feign-wrapper ``
offers a useful additional configurations that can be used to customize the execution of requests
also the handling of responses in some exceptional cases like :

* Add an undefined status in HttpStatus to manage it and to avoid the problem of ``No matching constant for [" + statusCode + "]``  
* Add the custom retry according to :
    * Series(INFORMATIONAL ,SUCCESSFUL ,REDIRECTION ,CLIENT_ERROR ,SERVER_ERROR) of the response
    * Http status(500, 503, 299) of the response  
* Define the max attempt and interval of retry

```yaml
feign-wrapper:
  customStatus: 299, 298 #List of unknown HttpStatus code
  retry:
    max-attempt: 4
    interval: 2000
    seriesSet: SERVER_ERROR #INFORMATIONAL ,SUCCESSFUL ,REDIRECTION ,CLIENT_ERROR ,SERVER_ERROR
    retryableStatusCodes: 400, 299 #List of HttpStatus code
```

## 2. Implementation

```java

@FeignClient(value = "test", url = "localhost:8080")
public interface MockClient {

    @GetMapping(value = "/v1/test")
    ResponseEntity<String> submit(@RequestHeader(name = "type") final int type);
}


@Slf4j
@Component
public class CommonErrorHandlerUtilImpl implements CommonResponseHandler {
    @Override
    public void handleSuccessResponse(ResponseWrap response) {
        log.info("The call was successful {}", response.body());
    }

    @Override
    public void handleHttpClientError(ResponseWrap response) {
        log.warn("A Client error occurred while calling the client {}", response.body());
    }

    @Override
    public void handleHttpServerError(ResponseWrap response) {
        log.warn("A Server error occurred while calling the client {}", response.body());
    }
}


@Service
public class TestService {
    @Autowired private MockClient mockClient;
    @Autowired private CommonErrorHandlerUtilImpl commonErrorHandlerUtilImpl;
    
    public void submitWithoutReturningResponse() {
        FeignResponseWrapper
                        .just(() -> mockClient.submit(500))
                        .doOnClientError(commonErrorHandlerUtilImpl::handleHttpClientError)
                        .doOnServerError(commonErrorHandlerUtilImpl::handleHttpServerError)
                        .doOnSuccess(commonErrorHandlerUtilImpl::handleSuccessResponse)
                        .then(() -> mockClient.submit(401))
                        .doOnClientError(commonErrorHandlerUtilImpl::handleHttpClientError)
                        .subscribe(
                                successResponse -> log.info("" + successResponse.body()),
                                errorResponse -> log.info("" + errorResponse.body()),
                                () -> log.info("## onComplete ##")
                        ); 
    }

    public ResponseEntity<Response> submit() {
            return FeignResponseWrapper
                            .just(() -> mockClient.submit(299))
                            .doOnClientError(commonErrorHandlerUtilImpl::handleHttpClientError)
                            .doOnServerError(commonErrorHandlerUtilImpl::handleHttpServerError)
                            .doOnSuccess(commonErrorHandlerUtilImpl::handleSuccessResponse)
                            .getBodyAsObject(Response.class)
                            .map(ResponseEntity::ok)
                            .orElse(ResponseEntity.badRequest().build()); 
    }
}

```
In this example we've configured a mock client to request the api `http://localhost:8080/v1/test`
and imported our Library to submit the type of http and benefit from a lot of 
filters provided by the Feign-wrapper in order to customize the processing of response based on
the response http code 

Result of submitWithoutReturningResponse :
```TEXT
WARN CustomRetryer                  : Feign retry attempt 1 due to  HttpStatus = 500, Series = Server error occurred
WARN CustomRetryer                  : Feign retry attempt 2 due to  HttpStatus = 500, Series = Server error occurred
WARN CustomRetryer                  : Feign retry attempt 3 due to  HttpStatus = 500, Series = Server error occurred
WARN CustomRetryer                  : Feign retry attempt 4 due to  HttpStatus = 500, Series = Server error occurred
WARN CommonErrorHandlerUtilImpl     : A Server error occurred while calling the client {"type":"Internal Server Error"}
ERROR FeignResponseWrapper          : The previous call has failed {} ResponseWrap{status=500, reason='Internal Server Error', headers={}, body='{"type":"Internal Server Error"}'}
INFO TestService                    : {"type":"Internal Server Error"}
INFO TestService                    : ## onComplete ##
```

Result of submit : 
```TEXT
WARN CustomRetryer                  : Feign retry attempt 1 due to  HttpStatus = 299, Series = SUCCESSFUL
WARN CustomRetryer                  : Feign retry attempt 2 due to  HttpStatus = 299, Series = SUCCESSFUL
WARN CustomRetryer                  : Feign retry attempt 3 due to  HttpStatus = 299, Series = SUCCESSFUL
WARN CustomRetryer                  : Feign retry attempt 4 due to  HttpStatus = 299, Series = SUCCESSFUL
INFO CommonErrorHandlerUtilImpl     : The call was successful {"type":"OK"}
```

Now let's go through the sequence that we have used one by one:

```TEXT
just : used to execute the request passed as a supplier in parameters 
then : execute another request after the current call
doOnClientError : call the method instance when the request fail due to client error with status 4XX
doOnServerError : call the method instance when the request fail due to server error with status 5XX
doOnSuccess : call the method instance when the request passe with status 2XX
subscribe : takes as arguments three anonymous functions 
    * First called when the request pass
    * Second : called only whe request fail due to client or server error
    * Third : called at the last
getBodyAsObject : convert and return the response to the target class type
```

### Dependencies :
 - feign form spring 3.3.0
 - feign okhttp 3.3.0
 - feign-form 3.3.0
 - Spring boot 2.4.2
