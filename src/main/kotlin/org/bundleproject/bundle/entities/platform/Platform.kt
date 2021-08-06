package org.bundleproject.bundle.entities.platform

enum class Platform(private val id: String) {
    Forge("forge"),
    Fabric("fabric");

    override fun toString(): String {
        return id
    }
}