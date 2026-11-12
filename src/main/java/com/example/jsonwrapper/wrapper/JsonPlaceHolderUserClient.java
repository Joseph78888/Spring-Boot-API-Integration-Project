package com.example.jsonwrapper.wrapper;

import com.example.jsonwrapper.model.User;
import com.example.jsonwrapper.service.UserGateway;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class JsonPlaceHolderUserClient implements UserGateway {

    private final RestClient restClient;

    public JsonPlaceHolderUserClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://jsonplaceholder.typicode.com")
                .build();
    }

    @Override
    public User[] getUsers() {
        return restClient.get()
                .uri("/users")
                .retrieve()
                .body(User[].class);
    }
}
