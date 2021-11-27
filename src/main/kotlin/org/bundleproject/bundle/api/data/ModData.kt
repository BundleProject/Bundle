package org.bundleproject.bundle.api.data

import org.bundleproject.libversion.Version

data class ModData(
    val url: String,
    val version: Version,
    val metadata: Metadata,
) {
    data class Metadata(
        val display: String,
        val creator: String,
    )
}