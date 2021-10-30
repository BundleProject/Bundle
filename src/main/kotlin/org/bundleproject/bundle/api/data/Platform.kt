package org.bundleproject.bundle.api.data

enum class Platform(val id: String) {
    Forge("forge"),
    Fabric("fabric");

    override fun toString(): String {
        return id
    }

    companion object {
        fun fromId(id: String): Platform? =
            values().find { it.id == id }
    }
}