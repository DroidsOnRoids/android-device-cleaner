@file:JvmName("CleanerApplication")

package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.AndroidDebugBridge

fun main(args: Array<String>) {
    val androidHome = System.getenv("ANDROID_HOME") ?: throw IllegalStateException("ANDROID_HOME environment variable is not set")
    AndroidDebugBridge.init(true)
    AndroidDebugBridge.createBridge("$androidHome/platform-tools/adb", true)

    val deviceCleaner = DeviceCleaner(args)
    AndroidDebugBridge.addDeviceChangeListener(deviceCleaner)
    val allDevicesCleaned = deviceCleaner.waitUntilAllDevicesCleaned()

    AndroidDebugBridge.terminate()
    if (!allDevicesCleaned) {
        System.exit(1)
    }
}