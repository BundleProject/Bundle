package org.bundleproject.bundle.utils

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.bundleproject.bundle.api.data.Platform

val gson = GsonBuilder().apply {
    applyGson(this)
}.create()

fun applyGson(builder: GsonBuilder) {
    builder.apply {
        registerTypeAdapter(Platform::class.java, PlatformTypeAdapter)
        registerTypeAdapter(Version::class.java, VersionTypeAdapter)
    }
}

object PlatformTypeAdapter : TypeAdapter<Platform>() {
    override fun write(out: JsonWriter?, value: Platform?) {
        out?.let {
            value?.let { out.value(it.id) } ?: out.nullValue()
        }
    }

    override fun read(`in`: JsonReader?): Platform {
        `in`?.let {
            return Platform.fromId(it.nextString()!!)!!
        } ?: error("The JsonReader given to the VersionTypeAdapter was null")
    }

}

object VersionTypeAdapter : TypeAdapter<Version>() {
    override fun write(out: JsonWriter?, value: Version?) {
        out?.let {
            value?.let { out.value(it.toString()) } ?: out.nullValue()
        }
    }

    override fun read(`in`: JsonReader?): Version {
        `in`?.let {
            return Version.of(it.nextString())
        } ?: error("The JsonReader given to the VersionTypeAdapter was null")
    }
}