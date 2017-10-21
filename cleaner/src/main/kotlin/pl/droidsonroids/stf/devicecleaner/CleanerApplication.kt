@file:JvmName("CleanerApplication")

package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.AndroidDebugBridge
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson

fun main(args: Array<String>) {
    val androidHome = System.getenv("ANDROID_HOME")
    val stfDeviceSerials = System.getenv("STF_DEVICE_SERIAL_LIST")
    val serialList = Gson().fromJson<List<String>>(stfDeviceSerials)

    AndroidDebugBridge.init(true)
    AndroidDebugBridge.createBridge("$androidHome/platform-tools/adb", true)

    val deviceCleaner = DeviceCleaner(serialList)
    AndroidDebugBridge.addDeviceChangeListener(deviceCleaner)
    val allDevicesCleaned = deviceCleaner.waitUntilAllDevicesCleaned()

    AndroidDebugBridge.terminate()
    if (!allDevicesCleaned) {
        System.exit(1)
    }
}