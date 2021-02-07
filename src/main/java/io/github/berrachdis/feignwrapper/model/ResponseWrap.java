package io.github.berrachdis.feignwrapper.model;

import feign.Request;
import feign.Response;
import io.github.berrachdis.feignwrapper.enumartion.Series;
import io.github.berrachdis.feignwrapper.util.CommonIOUtil;

import java.util.Collection;
import java.util.Map;

public class ResponseWrap {
    private final int status;
    private final String reason;
    private final Map<String, Collection<String>> headers;
    private final String body;
    private final Request request;
    private final Series series;

    public ResponseWrap(Response response) {
        this.status = response.status();
        this.reason = response.reason();
        this.body = CommonIOUtil.getResponseBody(response);
        this.request = response.request();
        this.headers = response.headers();
        this.series = Series.valueOf(response.status());
    }

    public int status() {
        return status;
    }

    public String reason() {
        return reason;
    }

    public Map<String, Collection<String>> headers() {
        return headers;
    }

    public String body() {
        return body;
    }

    public Request request() {
        return request;
    }

    public Series series() {
        return series;
    }

    public boolean is1xxInformational() {
        return Series.INFORMATIONAL.equals(this.series);
    }

    public boolean is2xxSuccessful() {
        return Series.SUCCESSFUL.equals(this.series);
    }

    public boolean is3xxRedirection() {
        return Series.REDIRECTION.equals(this.series);
    }

    public boolean is4xxClientError() {
        return Series.CLIENT_ERROR.equals(this.series);
    }

    public boolean is5xxServerError() {
        return Series.SERVER_ERROR.equals(this.series);
    }

    public boolean isError() {
        return is4xxClientError() || is5xxServerError();
    }

    @Override
    public String toString() {
        return "ResponseWrap{" +
                "status=" + status +
                ", reason='" + reason + '\'' +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                ", request=" + request +
                '}';
    }
}
