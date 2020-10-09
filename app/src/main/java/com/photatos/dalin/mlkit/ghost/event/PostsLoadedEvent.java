package com.photatos.dalin.mlkit.ghost.event;

import java.util.List;

import com.photatos.dalin.mlkit.ghost.model.entity.Post;

public class PostsLoadedEvent {

    public final List<Post> posts;
    public final int postsFetchLimit;

    public PostsLoadedEvent(List<Post> posts, int postsFetchLimit) {
        this.posts = posts;
        this.postsFetchLimit = postsFetchLimit;
    }

}
