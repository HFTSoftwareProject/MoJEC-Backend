package de.hftstuttgart.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Marcel Bochtler on 28.11.16.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CorruptedZipFileException extends RuntimeException {
    public CorruptedZipFileException(String s) {
        super(s);
    }
}
