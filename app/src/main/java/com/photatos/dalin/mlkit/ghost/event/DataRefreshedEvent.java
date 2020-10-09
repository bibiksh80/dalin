package com.photatos.dalin.mlkit.ghost.event;

import androidx.annotation.Nullable;

import com.photatos.dalin.mlkit.ghost.network.ApiFailure;

public class DataRefreshedEvent {

    public final ApiFailure apiFailure;

    public DataRefreshedEvent(@Nullable ApiFailure apiFailure) {
        this.apiFailure = apiFailure;
    }

}
