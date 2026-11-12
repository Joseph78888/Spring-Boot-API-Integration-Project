package com.example.jsonwrapper.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Post {
    Long id;
    String title;
    String body;
    Long post_id;
}
