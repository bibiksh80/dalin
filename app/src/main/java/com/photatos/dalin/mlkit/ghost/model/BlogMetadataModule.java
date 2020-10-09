package com.photatos.dalin.mlkit.ghost.model;

import io.realm.annotations.RealmModule;
import com.photatos.dalin.mlkit.ghost.model.entity.BlogMetadata;

// set of classes included in the schema for blog metadata Realm

@RealmModule(classes = {
        BlogMetadata.class
})
public class BlogMetadataModule {}
