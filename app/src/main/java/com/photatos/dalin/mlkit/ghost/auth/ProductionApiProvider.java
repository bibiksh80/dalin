package com.photatos.dalin.mlkit.ghost.auth;

import androidx.annotation.NonNull;

import com.photatos.dalin.mlkit.ghost.network.ApiProvider;
import com.photatos.dalin.mlkit.ghost.network.GhostApiService;
import com.photatos.dalin.mlkit.ghost.network.GhostApiUtils;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class ProductionApiProvider implements ApiProvider {

    private final Retrofit mRetrofit;

    public ProductionApiProvider(@NonNull OkHttpClient httpClient, @NonNull String blogUrl) {
        mRetrofit = GhostApiUtils.INSTANCE.getRetrofit(blogUrl, httpClient);
    }

    @Override
    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    @Override
    public GhostApiService getGhostApi() {
        return mRetrofit.create(GhostApiService.class);
    }

}
