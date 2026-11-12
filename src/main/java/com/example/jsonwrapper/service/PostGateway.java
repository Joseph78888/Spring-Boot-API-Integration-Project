package com.example.jsonwrapper.service;

import com.example.jsonwrapper.model.Post;
import com.example.jsonwrapper.model.User;

import java.util.List;

public interface PostGateway {

    Post[] getAllPosts();

    Post getPostById(Long id);

    Post createPost(Post post);

    Post[] getPostByUserId(Long userId);

    Post replacePost(Post post, Long id);

    void deletePost(Long id);
}
