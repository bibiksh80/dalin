package com.photatos.dalin.mlkit.ghost.auth;

import com.photatos.dalin.mlkit.ghost.network.entity.AuthReqBody;

/**
 * A persistent store of credentials where they may be saved for later retrieval via an
 * {@link CredentialSource}.
 *
 * The reason for separate "source" and "sink" interfaces is that a source may also be the login
 * screen where the user types in their credentials, while a sink is always a store on disk.
 */
interface CredentialSink {

    void saveCredentials(String blogUrl, AuthReqBody authReqBody);

    void deleteCredentials(String blogUrl);

    void setLoggedIn(String blogUrl, boolean isLoggedIn);

}
