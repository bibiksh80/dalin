package com.photatos.dalin.mlkit.ghost.model;

import io.realm.annotations.RealmModule;
import com.photatos.dalin.mlkit.ghost.model.entity.AuthToken;
import com.photatos.dalin.mlkit.ghost.model.entity.ConfigurationParam;
import com.photatos.dalin.mlkit.ghost.model.entity.ETag;
import com.photatos.dalin.mlkit.ghost.model.entity.PendingAction;
import com.photatos.dalin.mlkit.ghost.model.entity.Post;
import com.photatos.dalin.mlkit.ghost.model.entity.Role;
import com.photatos.dalin.mlkit.ghost.model.entity.Setting;
import com.photatos.dalin.mlkit.ghost.model.entity.Tag;
import com.photatos.dalin.mlkit.ghost.model.entity.User;

// set of classes included in the schema for blog data Realms

@RealmModule(classes = {
        AuthToken.class,
        ConfigurationParam.class,
        ETag.class,
        PendingAction.class,
        Post.class,
        Role.class,
        Setting.class,
        Tag.class,
        User.class
})
public class BlogDataModule {}
