package io.github.berrachdis.feignwrapper.model;

import io.github.berrachdis.feignwrapper.enumartion.Series;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class ResponseWrap {
    private final int status;
    private final String reason;
    private final Map<String, String> headers;
    private final String body;
    private final Series series;

    public ResponseWrap(ResponseEntity<String> response) {
        this.status = response.getStatusCodeValue();
        this.reason = HttpStatus.resolve(response.getStatusCodeValue()) == null ? "No matching constant for " + response.getStatusCodeValue() : response.getStatusCode().getReasonPhrase();
        this.body = response.getBody();
        this.headers = response.getHeaders().toSingleValueMap();
        this.series = Series.valueOf(response.getStatusCodeValue());
    }

    public int status() {
        return status;
    }

    public String reason() {
        return reason;
    }

    public String body() {
        return body;
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
                '}';
    }
}
