package com.photatos.dalin.mlkit.ghost.event;

import com.photatos.dalin.mlkit.ghost.model.entity.Post;

public class DeletePostEvent {

    public final Post post;

    public DeletePostEvent(Post post) {
        this.post = post;
    }

}
