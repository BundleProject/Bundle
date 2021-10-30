package org.bundleproject.bundle.api.responses

data class ErrorResponse(
    val success: Boolean,
    val error: String,
)
