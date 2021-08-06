package org.bundleproject.bundle.utils

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bundleproject.bundle.Bundle
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

fun URL.download(dest: File): File {
    if (dest.exists()) {
        dest.delete()
    } else {
        dest.parentFile?.mkdirs()
        dest.createNewFile()
    }

    openStream().use {
        dest.outputStream().use { os ->
            it.copyTo(os)
        }
    }

    return dest
}

fun getResourceImage(path: String): BufferedImage =
    ImageIO.read(Bundle::class.java.getResource(path))

fun launchCoroutine(name: String, block: suspend CoroutineScope.() -> Unit) =
    CoroutineScope(Dispatchers.IO + CoroutineName(name)).launch(block = block)
