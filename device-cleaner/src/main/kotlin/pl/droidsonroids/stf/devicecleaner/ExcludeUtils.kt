package pl.droidsonroids.stf.devicecleaner

import java.io.File
import java.util.regex.Pattern

fun parseExcludesFile(excludedListFilePath: String): Array<String> {
    val readText = File(excludedListFilePath).readText()
    val pattern = Pattern.compile("\\s+")
    return pattern.split(readText, 0)
}