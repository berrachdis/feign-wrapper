package io.github.berrachdis.feignwrapper.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import feign.Request;
import feign.Response;

@ExtendWith(SpringExtension.class)
class CommonIOUtilTest {
    private final Request request = Request.create(Request.HttpMethod.GET, anyString(), new HashMap<>(), "body".getBytes(),
            Charset.defaultCharset(), null);

    @Test
    void testGetResponseBodyWithGoodBody() {
        final String expectedResponse = "body ok";
        final Response response = Response.builder().request(request).body("body ok", Charset.defaultCharset()).build();
        final String actualResponse = CommonIOUtil.getResponseBody(response);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testGetResponseBodyWithNullBody() {
        final Response response = Response.builder().request(request).body(null, Charset.defaultCharset()).build();
        final String actualResponse = CommonIOUtil.getResponseBody(response);
        assertNull(actualResponse);
    }

    @Test
    void testGetResponseBodyWithNullResponse() {
        final String actualResponse = CommonIOUtil.getResponseBody(null);
        assertNull(actualResponse);
    }


    @Test
    void testGetResponseBodyWithInvalidResponseBody() {
        final Response response = Response.builder().request(request).body(new FailingInputStream(), 0).build();
        final String actualResponse = CommonIOUtil.getResponseBody(response);
        assertNull(actualResponse);
    }
}

class FailingInputStream extends InputStream {
    @Override
    public int read() throws IOException {
        throw new IOException("Test generated exception");
    }
}