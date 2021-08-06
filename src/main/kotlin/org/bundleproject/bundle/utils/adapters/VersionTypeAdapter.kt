package org.bundleproject.bundle.utils.adapters

import com.github.zafarkhaja.semver.Version
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

object VersionTypeAdapter : TypeAdapter<Version>() {
    override fun write(out: JsonWriter?, value: Version?) {
        out?.let {
            value?.let { out.value(it.toString()) } ?: out.nullValue()
        }
    }

    override fun read(`in`: JsonReader?): Version {
        `in`?.let {
            return Version.valueOf(it.nextString())
        } ?: error("The JsonReader given to the VersionTypeAdapter was null")
    }
}