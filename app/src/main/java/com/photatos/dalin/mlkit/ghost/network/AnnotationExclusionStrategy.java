package com.photatos.dalin.mlkit.ghost.network;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import com.photatos.dalin.mlkit.ghost.model.GsonExclude;

// mark fields with this custom annotation to make Gson ignore them
/* package */ class AnnotationExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(GsonExclude.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }

}
