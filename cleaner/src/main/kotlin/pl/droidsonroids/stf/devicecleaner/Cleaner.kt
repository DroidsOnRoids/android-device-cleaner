package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.AndroidDebugBridge

fun main(args: Array<String>) {
    AndroidDebugBridge.init(true)
    val androidHome = System.getenv("ANDROID_HOME")
    AndroidDebugBridge.createBridge("$androidHome/platform-tools/adb", true)

    AndroidDebugBridge.addDeviceChangeListener(DeviceCleaningListener())
}