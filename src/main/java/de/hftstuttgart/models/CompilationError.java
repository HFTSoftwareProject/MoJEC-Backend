package de.hftstuttgart.models;

import java.io.File;

public class CompilationError {

    private String code;
    private long columnNumber;
    private String kind;
    private long lineNumber;
    private String message;
    private long position;
    private File file;
    private long endPosition;
    private long startPosition;

    public CompilationError(String code, long columnNumber, String kind, long lineNumber, String message, long position, File file, long endPosition, long startPosition) {
        this.code = code;
        this.columnNumber = columnNumber;
        this.kind = kind;
        this.lineNumber = lineNumber;
        this.message = message;
        this.position = position;
        this.file = file;
        this.endPosition = endPosition;
        this.startPosition = startPosition;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(long columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }
}
