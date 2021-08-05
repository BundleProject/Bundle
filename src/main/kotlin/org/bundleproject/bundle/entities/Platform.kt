package org.bundleproject.bundle.entities

enum class Platform(private val id: String) {
    FORGE("forge"),
    FABRIC("fabric");

    override fun toString(): String {
        return id
    }
}