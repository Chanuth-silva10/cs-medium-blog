package io.chanuth.clubs.web;

import org.bson.types.ObjectId;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String what, String id) {
        super(what + " " + id + " does not exist");
    }

    /**
     * An id that is not a 24-character hex string can never match a document. Spring Data would
     * fail while converting it (a 500 for the client), so we answer "not found" before the query.
     */
    public static void ensureObjectId(String what, String id) {
        if (id == null || !ObjectId.isValid(id)) {
            throw new NotFoundException(what, String.valueOf(id));
        }
    }
}
