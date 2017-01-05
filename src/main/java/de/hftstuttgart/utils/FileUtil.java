package de.hftstuttgart.utils;

import java.io.File;

/**
 * Helper Class for all file related tasks.
 *
 * Created by Marcel Bochtler on 05.01.17.
 */
public class FileUtil {

    /**
     * Delete the folder and all containing files.
     * @param folder    Folder to delete
     */
    public static void deleteFolderRecursively(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolderRecursively(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

}
