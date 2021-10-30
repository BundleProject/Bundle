package org.bundleproject.bundle.api.data

import com.github.zafarkhaja.semver.Version

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