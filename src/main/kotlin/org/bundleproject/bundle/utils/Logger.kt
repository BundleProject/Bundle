package org.bundleproject.bundle.utils

const val RESET = "\u001b[0m" // Text Reset

const val BLACK = "\u001b[0;30m"
const val RED = "\u001b[0;31m"
const val GREEN = "\u001b[0;32m"
const val YELLOW = "\u001b[0;33m"
const val BLUE = "\u001b[0;34m"
const val PURPLE = "\u001b[0;35m"
const val CYAN = "\u001b[0;36m"
const val WHITE = "\u001b[0;37m"

fun info(any: Any?, newLine: Boolean = true) = print(RESET + "Bundle: ${any?.toString()}" + if (newLine) "\n" else "" + RESET)
fun err(any: Any?, newLine: Boolean = true) = print(RED + "Bundle: ${any?.toString()}" + if (newLine) "\n" else "" + RESET)
fun important(any: Any?, newLine: Boolean = true) = print(PURPLE + "Bundle: ${any?.toString()}" + if (newLine) "\n" else "" + RESET)
