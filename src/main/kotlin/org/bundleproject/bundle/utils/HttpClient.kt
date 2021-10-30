package org.bundleproject.bundle.utils

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*

val http = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = GsonSerializer {
            applyGson(this)
        }
    }
}