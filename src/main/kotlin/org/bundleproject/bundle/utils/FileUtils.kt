package org.bundleproject.bundle.utils

import java.io.File
import java.io.FileNotFoundException

object FileUtils {

    fun foreachFileDeep(rootDir: File, current: MutableList<File> = mutableListOf()): MutableList<File> {
        if (!rootDir.isDirectory) throw FileNotFoundException("File is not a directory!")

        for (file in rootDir.listFiles()!!) {
            if (file.isDirectory) foreachFileDeep(file, current)
            else current.add(file)
        }

        return current
    }

}