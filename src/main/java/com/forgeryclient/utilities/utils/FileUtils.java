package com.forgeryclient.utilities.utils;

import java.io.File;
import java.io.FilenameFilter;

public class FileUtils {

    public static void foreachDeep(File directory, FilenameFilter filter, FileAction action) {
        for (File f : directory.listFiles(filter)) {
            if (f.isDirectory()) {
                foreachDeep(f, filter, action);
            } else {
                action.run(f);
            }
        }
    }

    public interface FileAction {
        void run(File f);
    }

}