package com.photatos.dalin.mlkit.ghost.network;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import io.realm.RealmObject;

class RealmExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getDeclaringClass().equals(RealmObject.class);
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }

}
