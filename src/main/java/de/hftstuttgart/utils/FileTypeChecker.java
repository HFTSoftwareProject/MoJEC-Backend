package de.hftstuttgart.utils;

import com.google.common.io.Files;
import de.hftstuttgart.exceptions.FileTypeNotSupportedException;
import de.hftstuttgart.exceptions.NoZipFileException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

/**
 * Created by Marcel Bochtler on 29.11.16.
 */
public class FileTypeChecker {

    /**
     * Checks if the file is really a zip file.
     */
    public static void checkZipFile(File file) throws IOException {

        String fileExtension = Files.getFileExtension(file.getName());
        if (!"zip".equals(fileExtension)) {
            throw new FileTypeNotSupportedException("The file type " + fileExtension + " is not supported. Only 'zip' is supported.");
        }

        boolean isZipped = new ZipInputStream(new FileInputStream(file)).getNextEntry() != null;
        if (!isZipped) {
            throw new NoZipFileException("The file " + file.getAbsolutePath() + " seems to not be a zip file");
        }
    }
}
