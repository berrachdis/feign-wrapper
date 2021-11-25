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
## 1. Example

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
                        .just(() -> mockClient.submit(401))
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
                            .just(() -> mockClient.submit(CustomHttpStatus.OK.value()))
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
WARN CommonErrorHandlerUtilImpl    : A Client error occurred while calling the client {"type":"Unauthorized"}
ERROR FeignResponseWrapper         : The previous call has failed {} ResponseWrap{status=401, reason='Unauthorized', headers={}, body='{"type":"Unauthorized"}'}
WARN CommonErrorHandlerUtilImpl    : A Client error occurred while calling the client {"type":"Unauthorized"}
INFO TestService                   : {"type":"Unauthorized"}
INFO TestService                   : ## onComplete ##
```

Result of submit : 
```TEXT
INFO CommonErrorHandlerUtilImpl    : The call was successful {"type":"OK"}
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
