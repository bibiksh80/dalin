package com.photatos.dalin.mlkit.ghost.network.entity;

import java.util.Arrays;
import java.util.List;

import com.photatos.dalin.mlkit.ghost.model.entity.Post;

// dummy wrapper class needed for Retrofit
@SuppressWarnings({"WeakerAccess", "unused"})
public class PostStubList {

    public List<PostStub> posts;

    public static PostStubList from(PostStub... stubs) {
        PostStubList stubList = new PostStubList();
        stubList.posts = Arrays.asList(stubs);
        return stubList;
    }

    public static PostStubList from(Post post) {
        PostStubList stubList = new PostStubList();
        stubList.posts = Arrays.asList(new PostStub(post));
        return stubList;
    }

}
