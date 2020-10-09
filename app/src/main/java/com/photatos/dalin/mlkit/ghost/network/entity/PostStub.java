package com.photatos.dalin.mlkit.ghost.network.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import com.photatos.dalin.mlkit.ghost.model.entity.Post;
import com.photatos.dalin.mlkit.ghost.model.entity.Tag;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class PostStub {

    // field names should EXACTLY match those in Post class
    public final String title;
    public final String slug;
    public final String status;
    public final String mobiledoc;
    public final List<TagStub> tags;
    public final String featureImage;
    public final boolean featured;
    public final boolean page;
    public final String customExcerpt;

    public PostStub(@NonNull Post post) {
        this.title = post.getTitle();
        this.slug = post.getSlug();
        this.status = post.getStatus();
        this.mobiledoc = post.getMobiledoc();
        this.tags = new ArrayList<>(post.getTags().size());
        for (Tag tag : post.getTags()) {
            this.tags.add(new TagStub(tag));
        }
        this.featureImage = post.getFeatureImage();
        this.featured = post.isFeatured();
        this.page = post.isPage();
        this.customExcerpt = post.getCustomExcerpt();
    }

}
