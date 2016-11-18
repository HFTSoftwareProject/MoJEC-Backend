package de.hftstuttgart.utils;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Marcel Bochtler on 13.11.16.
 * See: https://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
 */
public class UnzipUtil {

    private static final Logger LOG = Logger.getLogger(UnzipUtil.class);

    public static List<File> unzip(File zipFile) {

        String outputFolder = zipFile.getParentFile().getAbsolutePath();
        List<File> unzippedFiles = new ArrayList<>();

        byte[] buffer = new byte[1024];
        try {
            //create output directory is not exists
            File folder = new File(zipFile.getAbsolutePath());
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {

                String fileName = zipEntry.getName();
                File unzippedFile = new File(outputFolder + File.separator + fileName);

                LOG.info("Unzipped file: " + unzippedFile.getName());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(unzippedFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(unzippedFile);

                int len;
                while ((len = zipInputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                zipEntry = zipInputStream.getNextEntry();

                unzippedFiles.add(unzippedFile);
            }

            zipInputStream.closeEntry();
            zipInputStream.close();

            zipFile.delete();

            return unzippedFiles;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
