package io.github.berrachdis.feignwrapper.model;

public class MockResponseDTO {
    private String mock;

    public MockResponseDTO() {}

    public MockResponseDTO(String mock) {
        this.mock = mock;
    }

    public String getMock() {
        return mock;
    }

    public void setMock(String mock) {
        this.mock = mock;
    }

    @Override
    public String toString() {
        return "MockResponseDTO{" +
                "mock='" + mock + '\'' +
                '}';
    }
}
