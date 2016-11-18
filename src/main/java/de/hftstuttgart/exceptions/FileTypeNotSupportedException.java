package de.hftstuttgart.exceptions;

/**
 * Created by Marcel Bochtler on 18.11.16.
 */
public class FileTypeNotSupportedException extends RuntimeException {

    public FileTypeNotSupportedException(String message) {
        super(message);
    }
}