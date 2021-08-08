package org.bundleproject.bundle.main

import com.github.zafarkhaja.semver.Version
import org.bundleproject.bundle.Bundle
import java.io.File

val entrypoints = arrayOf(
    "net.fabricmc.loader.launch.knot.KnotClient",
    "cpw.mods.bootstraplauncher.BootstrapLauncher",
    "net.minecraft.launchwrapper.Launch",
    "org.bookmc.loader.impl.launch.Quilt",
    "com.github.glassmc.loader.client.GlassClientMain",
    "net.minecraft.client.main.Main", // just in case user installed to vanilla
)

suspend fun main(args: Array<String>) {
    findEntrypoint().apply {
        val version = (args["version"] ?: "x.x.x").let {
            if (it == "MultiMC5") { "x.x.x" } else { it }
        }

        Bundle(File(args["gameDir"] ?: "."), Version.valueOf(version), "mods").start()
        getMethod("main", Array<String>::class.java).invoke(null, args)
    }
}

private operator fun Array<String>.get(arg: String) = getOrNull(indexOf("--$arg") + 1)

private fun findEntrypoint(): Class<*> {
    entrypoints.forEach {
        runCatching { Class.forName(it) }
            .getOrNull()
            ?.let { return it }
    }

    error("Bundle did not detect any known game entrypoints!")
}
