package com.photatos.dalin.mlkit.ghost.event;

public class PostDeletedEvent {

    public final String postId;

    public PostDeletedEvent(String postId) {
        this.postId = postId;
    }
}
