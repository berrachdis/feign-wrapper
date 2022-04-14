package io.github.berrachdis.feignwrapper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Contributor {
    private String login;
    private int contributions;

    public Contributor() {
    }

    public Contributor(String login, int contributions) {
        this.login = login;
        this.contributions = contributions;
    }

    public String getLogin() {
        return login;
    }

    public int getContributions() {
        return contributions;
    }
}