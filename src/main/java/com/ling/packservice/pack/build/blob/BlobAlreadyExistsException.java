package com.ling.packservice.pack.build.blob;

public class BlobAlreadyExistsException extends RuntimeException {
    public BlobAlreadyExistsException(String message) {
        super(message);
    }
}
