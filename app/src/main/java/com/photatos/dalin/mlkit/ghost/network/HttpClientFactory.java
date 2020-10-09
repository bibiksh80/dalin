package com.photatos.dalin.mlkit.ghost.network;

import androidx.annotation.Nullable;

import java.io.File;

import okhttp3.OkHttpClient;

public interface HttpClientFactory {

    OkHttpClient create(@Nullable File cacheDir);

}
