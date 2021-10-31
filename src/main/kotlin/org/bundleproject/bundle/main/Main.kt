package org.bundleproject.bundle.main

import org.bundleproject.bundle.Bundle
import org.bundleproject.bundle.utils.Version
import org.bundleproject.bundle.utils.important
import org.bundleproject.bundle.utils.info
import java.io.File

/**
 * List of known entrypoints for mod loaders.
 *
 * This is probably going to be removed in future
 * in favour of the installer providing the version
 * installation's mainclass as a program argument
 * rather than looking up a list.
 *
 * @since 0.0.1
 */
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
        try {
            val version = args["version"]
                ?.takeIf { it != "MultiMC5" }
                ?.let(Version::of)

            Bundle(File(args["gameDir"] ?: "."), version, "mods").start()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        this?.getMethod("main", Array<String>::class.java)?.invoke(null, args)
    }
}

private operator fun Array<String>.get(arg: String) = getNullOrNull(indexOf("--$arg").takeIf { it >= 0 }?.plus(1))
private fun Array<String>.getNullOrNull(index: Int?): String? {
    if (index == null) return null
    return getOrNull(index)
}

/**
 * Gets the first entrypoint available and returns it.
 *
 * If no entrypoints are found, it is presumed Bundle is
 * not running in a Minecraft context and returns no entrypoint.
 *
 * @since 0.0.1
 */
private fun findEntrypoint(): Class<*>? {
    entrypoints.forEach {
        runCatching { Class.forName(it) }
            .getOrNull()
            ?.let { return it }
    }

    important("Assuming development environment and returning no entrypoint.")
    println()
    return null
}
