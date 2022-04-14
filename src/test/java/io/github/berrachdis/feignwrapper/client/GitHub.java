package io.github.berrachdis.feignwrapper.client;

import org.springframework.http.ResponseEntity;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface GitHub {
    @RequestLine("GET /repos/{owner}/{repo}/contributors")
    @Headers("Content-Type: application/json")
    ResponseEntity<String> contributors(@Param("owner") String owner, @Param("repo") String repo);

    @RequestLine("GET /projects/{id}")
    @Headers("Content-Type: application/json")
    ResponseEntity<String> projects(@Param("id") Integer id);
}