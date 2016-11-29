package de.hftstuttgart.utils;

import de.hftstuttgart.exceptions.CorruptedZipFileException;
import de.hftstuttgart.exceptions.NoZipFileException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * Created by Marcel Bochtler on 13.11.16.
 * See: https://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
 */
public class UnzipUtil {

    private static final Logger LOG = Logger.getLogger(UnzipUtil.class);

    public static List<File> unzip(File zipFile) throws IOException {

        String outputFolder = zipFile.getParentFile().getAbsolutePath();
        List<File> unzippedFiles = new ArrayList<>();

        byte[] buffer = new byte[1024];

        //create output directory is not exists
        File folder = new File(zipFile.getAbsolutePath());
        if (!folder.exists()) {
            folder.mkdir();
        }

        //get the zip file content
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
        //get the zipped file list entry
        ZipEntry zipEntry = zipInputStream.getNextEntry();

        try {
            if (zipEntry == null) {
                throw new NoZipFileException("The file " + zipFile.getAbsolutePath() + " seems to not be a zip file");
            }
            while (zipEntry != null) {

                String fileName = zipEntry.getName();
                File unzippedFile = new File(outputFolder + File.separator + fileName);

                LOG.info("Unzipped file: " + unzippedFile.getName());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(unzippedFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(unzippedFile);

                int length;
                while ((length = zipInputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }

                fos.close();
                zipEntry = zipInputStream.getNextEntry();

                unzippedFiles.add(unzippedFile);
            }

            if (zipFile.exists()) {
                zipFile.delete();
            }

            return unzippedFiles;

        } catch (ZipException ze) {
            String msg = "Failed to unzip file " + zipFile;
            LOG.error(msg);
            throw new CorruptedZipFileException(msg);
        } finally {
            zipInputStream.closeEntry();
            zipInputStream.close();
        }
    }
}
