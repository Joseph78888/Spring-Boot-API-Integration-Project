package com.example.jsonwrapper.wrapper;

import com.example.jsonwrapper.exceptions.NotFoundException;
import com.example.jsonwrapper.exceptions.PostNotFoundException;
import com.example.jsonwrapper.model.Post;
import com.example.jsonwrapper.service.PostGateway;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class JsonPlaceHolderPostClient implements PostGateway {

    private final RestClient client;

    public JsonPlaceHolderPostClient(RestClient client) {
        this.client = client;

    }

    @Override
    public Post[] getAllPosts() {
        return client.get()
                .uri("/posts") // path variable || query parameter
                .retrieve()
                .body(Post[].class); // response body  || .toEntity ->
    }

    @Override
    public Post getPostById(Long id) {
        return client.get()
                .uri("/posts/{id}", id) // path variable || query parameter
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        (request, response) -> {
                            throw new PostNotFoundException(id);
                        }
                )
                .body(Post.class); // response body  || .toEntity ->
    }

    @Override
    public Post createPost(Post post) {
        return client.post()
                .uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(post)
                .retrieve()
                .body(Post.class);
    }

    @Override
    public Post[] getPostByUserId(Long userId) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/posts")
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .body(Post[].class);
    }

    @Override
    public Post replacePost(Post post, Long id) {
        return client.put()
                .uri("/posts/{id}", id)
                .body(post)
                .retrieve()
                .body(Post.class);
    }

    @Override
    public void deletePost(Long id) {
        client.delete()
                .uri("/posts/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }


}
