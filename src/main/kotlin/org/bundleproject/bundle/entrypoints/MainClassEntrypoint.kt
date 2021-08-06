package org.bundleproject.bundle.entrypoints

import com.github.zafarkhaja.semver.Version
import org.bundleproject.bundle.Bundle
import java.io.File

val ENTRY_POINTS = arrayOf(
    "net.fabricmc.loader.launch.knot.KnotClient",
    "cpw.mods.bootstraplauncher.BootstrapLauncher",
    "net.minecraft.launchwrapper.Launch",
    "net.minecraft.client.main.Main", // just in case user installed to vanilla
)

fun main(args: Array<String>) {
    var entryPoint: Class<*>? = null
    for (entryPointClassName in ENTRY_POINTS) {
        try {
            entryPoint = Class.forName(entryPointClassName)
            break
        } catch (e: ClassNotFoundException) {}
    }

    if (entryPoint == null) throw IllegalStateException("Bundle could not find entrypoint!")

    Bundle.start(
        File(getArgument(args, "gameDir") ?: "."),
        Version.valueOf((getArgument(args, "version") ?: "x.x.x").let { if (it.contentEquals("MultiMC5")) "x.x.x" else it }),
    )
    entryPoint.getMethod("main", Array<String>::class.java).invoke(null, args)
}

private fun getArgument(args: Array<String>, arg: String): String? = args.getOrNull(args.indexOf("--$arg") + 1)