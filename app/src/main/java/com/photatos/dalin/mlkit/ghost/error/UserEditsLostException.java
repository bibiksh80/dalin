package com.photatos.dalin.mlkit.ghost.error;

public final class UserEditsLostException extends RuntimeException {

    public UserEditsLostException() {
        super("USER EDITS WERE OVERWRITTEN! SEE STACK TO TRACE THE ROOT CAUSE.");
    }

}
