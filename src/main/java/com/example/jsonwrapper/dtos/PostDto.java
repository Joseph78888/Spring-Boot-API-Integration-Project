package com.example.jsonwrapper.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostDto {
    private Long id;
    private String title;

    public PostDto(String title, String body) {
    }

    public PostDto(Long postId, String title, String body) {
    }
}
