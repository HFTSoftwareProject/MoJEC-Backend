package de.hftstuttgart.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Marcel Bochtler on 18.11.16.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FileTypeNotSupportedException extends RuntimeException {

    public FileTypeNotSupportedException(String message) {
        super(message);
    }
}