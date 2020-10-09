package com.photatos.dalin.mlkit.ghost.event;

import com.photatos.dalin.mlkit.ghost.model.entity.Post;

public class SavePostEvent {

    public final Post post;
    public final boolean isAutoSave;    // was this post saved automatically or explicitly?

    public SavePostEvent(Post post, boolean isAutoSave) {
        this.post = post;
        this.isAutoSave = isAutoSave;
    }

}
