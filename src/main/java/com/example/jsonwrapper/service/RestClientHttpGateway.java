package com.example.jsonwrapper.service;

import org.springframework.web.client.RestClient;

import java.util.Map;

public class RestClientHttpGateway implements HttpHandller{

    private final RestClient restClient;

    public RestClientHttpGateway(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public <T> T get(String url, Class<T> responseType, Map<String, String> headers, Map<String, String> queryParams, Map<String, String> bodyParams) {
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(responseType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        return null;
    }

    @Override
    public <T> T post(String url, Object body, Class<T> responseType, Map<String, String> headers, Map<String, String> queryParams, Map<String, String> bodyParams) {
        return null;
    }

    @Override
    public <T> T delete(String url, Class<T> responseType, Map<String, String> headers, Map<String, String> queryParams, Map<String, String> bodyParams) {
        return null;
    }

    @Override
    public <T> T put(String url, Class<T> responseType, Map<String, String> headers) {
        return null;
    }
}
