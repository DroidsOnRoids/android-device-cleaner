package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.IDevice
import com.android.ddmlib.MultiLineReceiver
import com.android.ddmlib.NullOutputReceiver

val excludedPackages = arrayOf("jp.co.cyberagent.stf", "de.codenauts.hockeyapp", "pl.droidsonroids.devico",
        "com.ghisler.android.TotalCommander", "com.android.chrome", "com.facebook.katana", "pl.droidsonroids.devico", "com.instagram.android",
        "com.facebook.orca", "com.koushikdutta.vysor", "com.lexa.fakegps", "com.twitter.android", "com.google.android.apps.plus", "com.google.android.instantapps.supervisor")

fun IDevice.clean() {
    println("Cleaning device: $name")
    try {
        removeUnneededFiles()
        reboot(null)
        println("Device: $name clean")
    } catch (e: Exception) {
        println("Failed to clean device: $name")
        e.printStackTrace()
    }
}

fun IDevice.removeUnneededFiles() {
    executeShellCommand("pm list packages -3", object : MultiLineReceiver() {
        override fun processNewLines(lines: Array<out String>) {
            lines.map { it.removePrefix("package:") }
                    .filter { it.isNotBlank() && it !in excludedPackages }
                    .forEach {
                        println("Uninstalling package: $it")
                        uninstallPackage(it)
                    }
        }

        override fun isCancelled() = false
    })
    executeShellCommand("rm -rf /data/local/tmp/*", NullOutputReceiver.getReceiver())
    executeShellCommand("rm -rf /sdcard/*", NullOutputReceiver.getReceiver())
}