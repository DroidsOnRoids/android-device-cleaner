@file:JvmName("CleanerApplication")

package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.AndroidDebugBridge
import java.util.*

fun main(args: Array<String>) {
    println("Device serials: ${Arrays.toString(args)}")
    val androidHome = System.getenv("ANDROID_HOME") ?: throw IllegalStateException("ANDROID_HOME environment variable is not set")
    val excludedListFilePath =
        System.getenv("EXCLUDED_PACKAGES_LIST_PATH") ?: throw IllegalStateException("EXCLUDED_PACKAGES_LIST_PATH environment variable is not set")
    val excludedPackages = parseExcludesFile(excludedListFilePath)

    val deviceCleaner = DeviceCleaner(args, excludedPackages)
    AndroidDebugBridge.addDeviceChangeListener(deviceCleaner)

    AndroidDebugBridge.init(true)
    AndroidDebugBridge.createBridge("$androidHome/platform-tools/adb", true)

    val allDevicesCleaned = deviceCleaner.waitUntilAllDevicesCleaned()

    AndroidDebugBridge.terminate()
    if (!allDevicesCleaned) {
        System.exit(1)
    }
}