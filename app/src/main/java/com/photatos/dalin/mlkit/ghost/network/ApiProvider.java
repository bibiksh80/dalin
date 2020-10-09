package com.photatos.dalin.mlkit.ghost.network;

import retrofit2.Retrofit;

public interface ApiProvider {

    Retrofit getRetrofit();

    GhostApiService getGhostApi();

}
