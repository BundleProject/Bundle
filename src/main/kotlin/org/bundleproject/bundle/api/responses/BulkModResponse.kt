package org.bundleproject.bundle.api.responses

import org.bundleproject.bundle.api.data.ModData

data class BulkModResponse(
    val success: Boolean,
    val mods: List<ModData>,
)
