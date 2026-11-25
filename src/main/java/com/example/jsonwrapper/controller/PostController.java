package com.example.jsonwrapper.controller;

import com.example.jsonwrapper.dtos.PostDto;
import com.example.jsonwrapper.model.Post;
import com.example.jsonwrapper.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }


    @GetMapping
    public Post[] getAllPosts(@RequestParam(required = false) Long userId){

        if (userId != null) {
            return postService.getPostByUserId(userId);
        }

        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id){
        return postService.getPostById(id);
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post){

        Post createPost = postService.createPost(post);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createPost);
    }


    @PutMapping("/{id}")
    public Post replacePost(@RequestBody Post post, @PathVariable Long id){
        return postService.replacePost(id, post);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id){
        postService.deletePost(id);
    }
}
