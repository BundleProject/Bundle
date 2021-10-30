package org.bundleproject.bundle.api.responses

import org.bundleproject.bundle.api.data.ModData

data class ModResponse(
    val success: Boolean,
    val data: ModData,
)
