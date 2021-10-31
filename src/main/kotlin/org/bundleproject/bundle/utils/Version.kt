package org.bundleproject.bundle.utils

/**
 * A simple version util to compare versions.
 *
 * This parser is not semver compliant. It is trying to be as
 * lenient as possible to prevent parse failure.
 *
 * @since 0.1.0
 */
class Version(
    val major: Int,
    val minor: Int = 0,
    val patch: Int = 0,
    val build: Int = 0,

    val prerelease: Int? = null,
    val revision: String? = null,
) {
    operator fun compareTo(other: Version): Int {
        var result = major - other.major
        if (result == 0) {
            result = minor - other.minor
            if (result == 0) {
                result = patch - other.patch
                if (result == 0) {
                    result = build - other.build
                    if (result == 0) {
                        result = (prerelease ?: Int.MAX_VALUE) - (other.prerelease ?: Int.MAX_VALUE)
                    }
                }
            }
        }

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Version) return false

        return compareTo(other) == 0
    }

    override fun toString(): String {
        var str = "$major.$minor.$patch"
        if (build != 0) str += ".$build"
        if (prerelease != null) str += "-pre.$prerelease"
        if (revision != null) str += "-rev.$revision"

        return str
    }

    override fun hashCode(): Int {
        var result = major
        result = 31 * result + minor
        result = 31 * result + patch
        result = 31 * result + build
        result = 31 * result + (prerelease ?: 0)
        result = 31 * result + (revision?.hashCode() ?: 0)
        return result
    }

    companion object {
        val CORE_REGEX = Regex("(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+)(?:\\.(?<build>\\d+))?)?)?")
        val PRE_REGEX = Regex("pre(?:release)?[.+-]?(?<prerelease>\\d+)")
        val REV_REGEX = Regex("rev[.+-]?(?<revision>[a-f\\d]+)")

        fun of(version: String): Version {
            val coreMatch = CORE_REGEX.find(version) ?: throw VersionParseException("Not a legal version string")
            val preMatch = PRE_REGEX.find(version)
            val revMatch = REV_REGEX.find(version)

            val major = coreMatch.groups["major"]!!.value.toInt()
            val minor = coreMatch.groups["minor"]?.value?.toInt() ?: 0
            val patch = coreMatch.groups["patch"]?.value?.toInt() ?: 0
            val build = coreMatch.groups["build"]?.value?.toInt() ?: 0

            val prerelease = preMatch?.groups?.get("prerelease")?.value?.toInt()
            val revision = revMatch?.groups?.get("revision")?.value

            return Version(major, minor, patch, build, prerelease, revision)
        }
    }

    class VersionParseException(message: String) : IllegalArgumentException(message)
}