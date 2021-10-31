package org.bundleproject.bundle.entities

import org.bundleproject.bundle.api.data.Platform
import org.bundleproject.bundle.utils.Version

class RemoteMod(
    enabled: Boolean = true,
    name: String,
    id: String,
    version: Version,
    minecraftVersion: Version,
    fileName: String,
    platform: Platform,
    val downloadUrl: String,
) : Mod(enabled, name, id, version, minecraftVersion, fileName, platform)