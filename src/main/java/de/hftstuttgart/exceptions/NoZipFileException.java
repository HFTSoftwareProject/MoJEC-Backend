package de.hftstuttgart.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Marcel Bochtler on 29.11.16.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoZipFileException extends RuntimeException {
    public NoZipFileException(String message) {
        super(message);
    }
}
