package com.example.jsonwrapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    Long id;
    String title;
    String body;

    @JsonProperty("userId")
    Long userId;

//    public Post(String title, String body,long userId) {
//    }
}
