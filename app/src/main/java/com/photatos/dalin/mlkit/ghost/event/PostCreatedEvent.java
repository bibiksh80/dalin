package com.photatos.dalin.mlkit.ghost.event;

import com.photatos.dalin.mlkit.ghost.model.entity.Post;

public class PostCreatedEvent {

    public final Post newPost;

    public PostCreatedEvent(Post newPost) {
        this.newPost = newPost;
    }

}
