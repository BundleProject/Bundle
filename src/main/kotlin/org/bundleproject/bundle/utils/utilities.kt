package org.bundleproject.bundle.utils

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bundleproject.bundle.Bundle
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

/**
 * Downloads a file from a url using Ktor
 *
 * @return if the download was successful
 * @since 0.1.0
 */
suspend fun HttpClient.downloadFile(file: File, url: String): Boolean {
    val call = request<HttpResponse> {
        url(url)
        method = HttpMethod.Get
    }

    if (!call.status.isSuccess())
        return false

    call.content.copyAndClose(file.writeChannel())

    return true
}

fun getResourceImage(path: String): BufferedImage =
    ImageIO.read(Bundle::class.java.getResource(path))

fun getFileNameFromUrl(url: String): String =
    url.decodeURLPart(url.lastIndexOf('/') + 1)

fun <T> Iterable<T>.toFormattedString(): String {
    return "[${this.joinToString(", ") { it.toString() }}]"
}

fun String.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(this.toByteArray())
}
