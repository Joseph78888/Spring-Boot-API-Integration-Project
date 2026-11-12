package com.example.jsonwrapper.service;

import com.example.jsonwrapper.exceptions.ApiException;
import com.example.jsonwrapper.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Value("${jsonPlaceHolder.base-url}")
    private String baseUrl;

    @Bean
    public RestClient jsonPlaceHolderClient(RestClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                        throw new NotFoundException("Resource not found");
                    }
                    throw new ApiException("Unexpected error", response.getStatusCode());
                })
                .build();
        }
    }

