package com.photatos.dalin.mlkit.ghost.event;

import com.photatos.dalin.mlkit.ghost.model.entity.User;

public class UserLoadedEvent {

    public final User user;

    public UserLoadedEvent(User user) {
        this.user = user;
    }

}
