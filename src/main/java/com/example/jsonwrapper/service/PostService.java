package com.example.jsonwrapper.service;

import com.example.jsonwrapper.dtos.PostDto;
import com.example.jsonwrapper.model.Post;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostGateway postGateway;

    public PostService(PostGateway postGateway) {
        this.postGateway = postGateway;
    }

    public Post[] getAllPosts() {
        return postGateway.getAllPosts();

    }

    public Post getPostById(Long id) {

        return postGateway.getPostById(id);

//        System.out.println(post.getTitle() + " lllllllllllllllllllll " + post.getBody() + "  " + post.getPost_id());

//        return new PostDto(
//                post.getTitle(),
//                post.getBody()
//        );

    }

    public Post[] getPostByUserId(Long userId) {
        return postGateway.getPostByUserId(userId);
    }

    public Post createPost(Post post){
        return postGateway.createPost(post);
    }

    public Post replacePost(Long id, Post post){
        return postGateway.replacePost(post, id);
    }

    public void deletePost(Long id){
        postGateway.deletePost(id);
    }


}
