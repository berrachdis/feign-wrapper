package io.github.berrachdis.feignwrapper.propertie;

import io.github.berrachdis.feignwrapper.enumartion.Series;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "feign-wrapper")
public class FeignWrapperProperties {
    private final Retry retry = new Retry();

    public Retry getRetry() {
        return retry;
    }

    public static class Retry {
        private long interval = 2000L;
        private int maxAttempt = 1;
        private final Set<Series> seriesSet = new HashSet<>();
        private final Set<Integer> retryableStatusCodes = new HashSet<>();

        public long getInterval() {
            return interval;
        }

        public int getMaxAttempt() {
            return maxAttempt;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }

        public void setMaxAttempt(int maxAttempt) {
            this.maxAttempt = maxAttempt;
        }

        public Set<Series> getSeriesSet() {
            return seriesSet;
        }

        public Set<Integer> getRetryableStatusCodes() {
            return retryableStatusCodes;
        }
    }
}
