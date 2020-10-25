@file:JvmName("CleanerApplication")

package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.AndroidDebugBridge
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.system.exitProcess

suspend fun main(args: Array<String>) {
    println("Device serials: ${args.contentToString()}")
    val androidHome = System.getenv("ANDROID_HOME") ?: throw IllegalStateException("ANDROID_HOME environment variable is not set")
    val excludedListFilePath =
        System.getenv("EXCLUDED_PACKAGES_LIST_PATH") ?: throw IllegalStateException("EXCLUDED_PACKAGES_LIST_PATH environment variable is not set")
    val excludedPackages = parseExcludesFile(excludedListFilePath)

    val deviceCleaner = DeviceCleaner(args, excludedPackages)
    AndroidDebugBridge.addDeviceChangeListener(deviceCleaner)

    AndroidDebugBridge.init(true)
    AndroidDebugBridge.createBridge("$androidHome/platform-tools/adb", true, 30, SECONDS)

    val allDevicesCleaned = deviceCleaner.cleanAllDevices()

    AndroidDebugBridge.terminate()
    if (!allDevicesCleaned) {
        exitProcess(1)
    }
}