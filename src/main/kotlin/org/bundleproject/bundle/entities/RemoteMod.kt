package org.bundleproject.bundle.entities

import com.github.zafarkhaja.semver.Version
import org.bundleproject.bundle.api.data.Platform

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