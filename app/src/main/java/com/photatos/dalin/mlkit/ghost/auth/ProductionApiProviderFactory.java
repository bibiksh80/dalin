package com.photatos.dalin.mlkit.ghost.auth;

import com.photatos.dalin.mlkit.ghost.network.ApiProvider;
import com.photatos.dalin.mlkit.ghost.network.ApiProviderFactory;
import okhttp3.OkHttpClient;

class ProductionApiProviderFactory implements ApiProviderFactory {

    private final OkHttpClient mHttpClient;

    public ProductionApiProviderFactory(OkHttpClient httpClient) {
        mHttpClient = httpClient;
    }

    @Override
    public ApiProvider create(String blogUrl) {
        return new ProductionApiProvider(mHttpClient, blogUrl);
    }

}
