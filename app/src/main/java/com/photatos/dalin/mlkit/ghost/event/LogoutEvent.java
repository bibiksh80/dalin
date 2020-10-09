package com.photatos.dalin.mlkit.ghost.event;

import androidx.annotation.NonNull;

public class LogoutEvent {

    @NonNull public final String blogUrl;
    public final boolean forceLogout;

    public LogoutEvent(@NonNull String blogUrl, boolean forceLogout) {
        this.blogUrl = blogUrl;
        this.forceLogout = forceLogout;
    }

}
