package com.photatos.dalin.mlkit.ghost.event;

import java.util.List;

import com.photatos.dalin.mlkit.ghost.model.entity.Tag;

public class TagsLoadedEvent {

    public final List<Tag> tags;

    public TagsLoadedEvent(List<Tag> tags) {
        this.tags = tags;
    }

}
