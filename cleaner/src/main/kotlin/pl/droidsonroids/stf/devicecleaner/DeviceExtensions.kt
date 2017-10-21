package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.IDevice
import com.android.ddmlib.NullOutputReceiver

private val excludedPackages = rawExcludedPackages.split("\\s+".toRegex())

fun IDevice.clean(): Boolean {
    println("Cleaning device: $name")
    try {
        removeUnneededFiles()
        reboot(null)
        println("Device: $name cleaned")
        return true
    } catch (e: Exception) {
        println("Failed to clean device: $name, error: ${e.message}")
    }
    return false
}

fun IDevice.removeUnneededFiles() {
    executeShellCommand("pm list packages -3", NonCancellableMultilineReceiver { packageLines ->
        packageLines.map { it.removePrefix("package:") }
                .filter { it.isNotBlank() && it !in excludedPackages }
                .forEach {
                    println("Uninstalling package: $it")
                    uninstallPackage(it)
                }
    })
    executeShellCommand("rm -rf /data/local/tmp/*", NullOutputReceiver.getReceiver())
    executeShellCommand("rm -rf /sdcard/*", NullOutputReceiver.getReceiver())
}