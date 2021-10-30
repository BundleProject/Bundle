package org.bundleproject.bundle.utils

import java.util.*

fun String.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(this.toByteArray())
}