package com.example.jsonwrapper.e2e;


import com.example.jsonwrapper.model.Post;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class PostsApiE2ETest {

    @Autowired
    private TestRestTemplate rest;

    // Post has no all-args constructor, so we build it with setters
    private Post newPost(long userId, String title, String body) {
        Post p = new Post();
        p.setBody(body);
        p.setTitle(title);
        p.setUserId(userId);

        return p;
    }

    @Test
    void getPostById_returnsPost() {
        ResponseEntity<Post> res = rest.getForEntity("/api/posts/1", Post.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().getId()).isEqualTo(1L);
        assertThat(res.getBody().getUserId()).isEqualTo(1L);
        assertThat(res.getBody().getTitle()).isNotBlank();
    }

    @Test
    void getAllPosts_returns100Posts() {
        ResponseEntity<Post[]> res = rest.getForEntity("/api/posts", Post[].class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).hasSize(100);
    }

    @Test
    void getAllPosts_withUserId_returnsOnlyThatUsersPosts() {
        ResponseEntity<Post[]> res = rest.getForEntity("/api/posts?userId=1", Post[].class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).hasSize(10);
        assertThat(res.getBody()).extracting(Post::getUserId).containsOnly(1L);
    }

    // ---------- POST /api/posts ----------

    @Test
    void createPost_returnsCreatedPost() {
        ResponseEntity<Post> res = rest.postForEntity(
                "/api/posts", newPost(1, "foo", "bar"), Post.class);

        // CHECK: change to OK if controller returns 200
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody().getId()).isEqualTo(101L);
        assertThat(res.getBody().getTitle()).isEqualTo("foo");
    }

    // ---------- PUT /api/posts/{id} ----------

    @Test
    void replacePost_returnsUpdatedPost() {
        ResponseEntity<Post> res = rest.exchange(
                "/api/posts/1", HttpMethod.PUT,
                new HttpEntity<>(newPost(1, "new title", "new body")), Post.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().getTitle()).isEqualTo("new title");
    }

    // ---------- DELETE /api/posts/{id} ----------

    @Test
    void deletePost_returns200() {
        ResponseEntity<Void> res = rest.exchange(
                "/api/posts/1", HttpMethod.DELETE, null, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    void getPostById_returns404_whenMissing() {
        ResponseEntity<String> res = rest.getForEntity("/api/posts/9999", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


}
